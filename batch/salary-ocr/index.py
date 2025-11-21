import json
import logging
import os
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Tuple

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from openai import OpenAI


# ログ設定
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)

BASE_DIR = Path(__file__).resolve().parent
PROMPTS_DIR = BASE_DIR / "prompts"


@dataclass
class OcrTaskMessage:
    """SQSメッセージ"""
    ocr_task_id: str
    user_id: str
    target_date: str
    file_id: str


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def parse_sqs_message(body: str) -> OcrTaskMessage:
    """SQSメッセージをドメインオブジェクトに変換"""
    data = json.loads(body)
    return OcrTaskMessage(
        ocr_task_id=data["ocrTaskId"],
        user_id=data["userId"],
        target_date=data["targetDate"],
        file_id=data["fileId"],
    )


def build_s3_key(user_id: str, file_id: str) -> str:
    """S3オブジェクトキーを生成"""
    return f"{user_id}/{file_id}.pdf"


def get_boto3_clients() -> Dict[str, Any]:
    """AWSクライアントを取得"""
    region = get_env_or_raise("AWS_REGION")
    session = boto3.Session(region_name=region)
    return {
        "sqs": session.client("sqs"),
        "s3": session.client("s3"),
        "ssm": session.client("ssm"),
        "dynamodb": session.resource("dynamodb"),
    }


def get_env_or_raise(name: str) -> str:
    """環境変数を取得"""
    value = os.getenv(name)
    if not value:
        raise RuntimeError(
            f"Environment variable {name} is required but not set")
    return value


def update_task_status(
    dynamodb,
    table_name: str,
    task_id: str,
    status: str,
) -> None:
    """salary_ocr_tasksテーブルを更新"""
    table = dynamodb.Table(table_name)
    logger.info("Updating task %s status to %s", task_id, status)
    table.update_item(
        Key={"taskId": task_id},
        UpdateExpression="SET #s = :s, updatedAt = :u",
        ExpressionAttributeNames={"#s": "status"},
        ExpressionAttributeValues={
            ":s": status,
            ":u": now_iso(),
        },
    )


def download_pdf_from_s3(s3, bucket_name: str, key: str) -> bytes:
    """S3からPDFバイナリを取得"""
    logger.info("Downloading payslip from S3 bucket=%s key=%s",
                bucket_name, key)
    try:
        resp = s3.get_object(Bucket=bucket_name, Key=key)
        return resp["Body"].read()
    except (BotoCoreError, ClientError) as e:
        logger.exception("Failed to download file from S3")
        raise RuntimeError("Failed to download file from S3") from e


def get_openai_api_key_from_ssm(ssm, param_name: str) -> str:
    """SSMパラメータストアからOpenAI APIキーを取得"""
    logger.info(
        "Fetching OpenAI API key from SSM Parameter Store: %s", param_name)
    try:
        resp = ssm.get_parameter(
            Name=param_name,
            WithDecryption=True,
        )
        return resp["Parameter"]["Value"]
    except (BotoCoreError, ClientError) as e:
        logger.exception("Failed to fetch OpenAI API key from SSM")
        raise RuntimeError("Failed to fetch OpenAI API key from SSM") from e


def load_prompts() -> Tuple[str, str]:
    """
    prompts/system.md, prompts/user.md, prompts/sample.json を読み込み、
    user.md 内の {{ONE_SHOT_JSON}} を sample.json で置換した文字列を返す。
    """
    system_path = PROMPTS_DIR / "system.md"
    user_path = PROMPTS_DIR / "user.md"
    sample_path = PROMPTS_DIR / "sample.json"

    try:
        system_prompt = system_path.read_text(encoding="utf-8")
        user_template = user_path.read_text(encoding="utf-8")
        sample_json = sample_path.read_text(encoding="utf-8").strip()
    except OSError as e:
        logger.exception("Failed to load prompt files from %s", PROMPTS_DIR)
        raise RuntimeError("Failed to load prompt files") from e

    user_prompt = user_template.replace("{{ONE_SHOT_JSON}}", sample_json)
    return system_prompt, user_prompt


def call_openai_ocr(
    pdf_bytes: bytes,
    api_key: str,
    model: str = "gpt-5.1",
) -> Dict[str, Any]:
    """OpenAI API に対してOCRを呼び出し、構造化JSONを取得"""
    logger.info("Calling OpenAI OCR (dummy implementation)")

    system_prompt, user_prompt = load_prompts()
    max_attempts = int(get_env_or_raise("OPENAI_OCR_MAX_ATTEMPTS"))
    client = OpenAI(api_key=api_key, max_retries=max_attempts)

    # ファイルアップロード
    file_obj = client.files.create(
        file=("payslip.pdf", pdf_bytes, "application/pdf"),
        purpose="assistants",
    )

    # OCRリクエスト
    response = client.responses.create(
        model=model,
        input=[
            {
                "role": "system",
                "content": [
                    {
                        "type": "input_text",
                        "text": system_prompt,
                    },
                ],
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": user_prompt,
                    },
                    {
                        "type": "input_file",
                        "file_id": file_obj.id,
                    },
                ],
            },
        ],
    )

    # 結果からJSON抽出
    raw_json = response.output_text
    logger.debug("Raw OCR JSON: %s", raw_json)
    result: Dict[str, Any] = json.loads(raw_json)
    return result


def save_salary(
    dynamodb,
    table_name: str,
    user_id: str,
    target_date: str,
    ocr_result: Dict[str, Any],
) -> str:
    """OCR結果をsalariesテーブルに登録"""
    table = dynamodb.Table(table_name)
    salary_id = str(uuid.uuid4())
    item: Dict[str, Any] = {
        "salaryId": salary_id,
        "userId": user_id,
        "targetDate": target_date,
        "overview": ocr_result.get("overview", {}),
        "structure": ocr_result.get("structure", {}),
        "payslipData": ocr_result.get("payslipData", []),
    }
    logger.info("Saving salary: salaryId=%s userId=%s targetDate=%s",
                salary_id, user_id, target_date)
    table.put_item(Item=item)
    return salary_id


def process_ocr_task(msg: OcrTaskMessage) -> None:
    """1件のOCRタスクメッセージを処理するメインフロー"""
    clients = get_boto3_clients()
    dynamodb = clients["dynamodb"]
    s3 = clients["s3"]
    ssm = clients["ssm"]

    salary_table_name = get_env_or_raise("SALARIES_TABLE_NAME")
    ocr_task_table_name = get_env_or_raise("SALARY_OCR_TASKS_TABLE_NAME")
    uploads_bucket_name = get_env_or_raise("UPLOADS_BUCKET_NAME")
    openai_param_name = get_env_or_raise("OPENAI_API_KEY_SSM_NAME")
    openai_model = get_env_or_raise("OPENAI_MODEL")

    # RUNNING に更新
    try:
        update_task_status(dynamodb, ocr_task_table_name, msg.ocr_task_id, "RUNNING")
    except Exception:
        logger.exception("Failed to update task status to RUNNING")
        raise

    try:
        # PDFダウンロード
        s3_key = build_s3_key(msg.user_id, msg.file_id)
        pdf_bytes = download_pdf_from_s3(s3, uploads_bucket_name, s3_key)

        # APIキー取得
        api_key = get_openai_api_key_from_ssm(ssm, openai_param_name)

        # OCR実行
        ocr_result = call_openai_ocr(pdf_bytes, api_key, model=openai_model)

        # salariesに登録
        save_salary(
            dynamodb=dynamodb,
            table_name=salary_table_name,
            user_id=msg.user_id,
            target_date=msg.target_date,
            ocr_result=ocr_result,
        )

        # COMPLETEDに更新
        update_task_status(dynamodb, ocr_task_table_name, msg.ocr_task_id, "COMPLETED")
    except Exception as e:
        logger.exception("Failed to process OCR task %s", msg.ocr_task_id)
        # FAILEDに更新
        try:
            update_task_status(dynamodb, ocr_task_table_name, msg.ocr_task_id, "FAILED")
        except Exception:
            logger.exception("Failed to update task status to FAILED")
        raise e


def lambda_handler(event, _):
    records: List[Dict[str, Any]] = event.get("Records", [])
    if not records:
        logger.info("No Records in event.")
        return {"batchItemFailures": []}

    failures: List[Dict[str, str]] = []

    for record in records:
        message_id = record.get("messageId")
        body = record.get("body", "")
        logger.info("Received SQS messageId=%s body=%s", message_id, body)

        try:
            msg: OcrTaskMessage = parse_sqs_message(body)
            process_ocr_task(msg)
        except Exception:
            logger.exception("Failed to process messageId=%s", message_id)
            if message_id:
                # 失敗したメッセージだけ再実行対象にする
                failures.append({"itemIdentifier": message_id})

    # ReportBatchItemFailures形式のレスポンス
    return {"batchItemFailures": failures}
