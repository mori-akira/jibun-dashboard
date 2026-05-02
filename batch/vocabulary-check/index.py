import json
import logging
import os
import time
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

import boto3
from botocore.exceptions import BotoCoreError, ClientError

logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
    force=True,
)
logger = logging.getLogger(__name__)

BASE_DIR = Path(__file__).resolve().parent
PROMPTS_DIR = BASE_DIR / "prompts"
BEDROCK_MAX_ATTEMPTS = 3


def get_env_or_raise(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"Environment variable {name} is required but not set")
    return value


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def load_prompts() -> Tuple[str, str]:
    """system.md と user.md を読み込む"""
    try:
        system_prompt = (PROMPTS_DIR / "system.md").read_text(encoding="utf-8")
        user_template = (PROMPTS_DIR / "user.md").read_text(encoding="utf-8")
    except OSError as e:
        logger.exception("Failed to load prompt files from %s", PROMPTS_DIR)
        raise RuntimeError("Failed to load prompt files") from e
    return system_prompt, user_template


def get_all_users(dynamodb, users_table_name: str) -> List[str]:
    """usersテーブルをスキャンして全userIdを返す"""
    table = dynamodb.Table(users_table_name)
    user_ids: List[str] = []
    kwargs: Dict[str, Any] = {"ProjectionExpression": "userId"}
    while True:
        response = table.scan(**kwargs)
        user_ids.extend(item["userId"] for item in response.get("Items", []))
        last_key = response.get("LastEvaluatedKey")
        if not last_key:
            break
        kwargs["ExclusiveStartKey"] = last_key
    return user_ids


def get_vocabularies_for_user(dynamodb, vocabularies_table_name: str, user_id: str) -> List[Dict[str, Any]]:
    """ユーザーのボキャブラリー全件を取得"""
    table = dynamodb.Table(vocabularies_table_name)
    items: List[Dict[str, Any]] = []
    kwargs: Dict[str, Any] = {
        "KeyConditionExpression": "userId = :uid",
        "ExpressionAttributeValues": {":uid": user_id},
    }
    while True:
        response = table.query(**kwargs)
        items.extend(response.get("Items", []))
        last_key = response.get("LastEvaluatedKey")
        if not last_key:
            break
        kwargs["ExclusiveStartKey"] = last_key
    return items


def get_vocabulary_tags_for_user(dynamodb, vocabulary_tags_table_name: str, user_id: str) -> List[str]:
    """ユーザーのボキャブラリータグ名一覧を取得"""
    table = dynamodb.Table(vocabulary_tags_table_name)
    tag_names: List[str] = []
    kwargs: Dict[str, Any] = {
        "KeyConditionExpression": "userId = :uid",
        "ExpressionAttributeValues": {":uid": user_id},
        "ProjectionExpression": "vocabularyTag",
    }
    while True:
        response = table.query(**kwargs)
        tag_names.extend(item["vocabularyTag"] for item in response.get("Items", []))
        last_key = response.get("LastEvaluatedKey")
        if not last_key:
            break
        kwargs["ExclusiveStartKey"] = last_key
    return tag_names


def get_check_result(
    dynamodb,
    check_results_table_name: str,
    user_id: str,
    vocabulary_id: str,
) -> Optional[Dict[str, Any]]:
    """チェック結果テーブルから既存レコードを取得"""
    table = dynamodb.Table(check_results_table_name)
    response = table.get_item(Key={"userId": user_id, "vocabularyId": vocabulary_id})
    return response.get("Item")


def needs_check(vocabulary: Dict[str, Any], check_result: Optional[Dict[str, Any]]) -> bool:
    """チェックが必要かどうかを判定する"""
    if check_result is None:
        return True
    checked_at = datetime.fromisoformat(check_result["checkedAt"])
    updated_at = datetime.fromisoformat(vocabulary["updatedDateTime"])
    return updated_at > checked_at


def build_user_prompt(
    user_template: str,
    vocabulary: Dict[str, Any],
    all_vocabulary_names: List[str],
    all_tag_names: List[str],
    vocabulary_tag_names: List[str],
) -> str:
    """ユーザープロンプトを構築する"""
    names_text = "\n".join(f"- {name}" for name in all_vocabulary_names)
    tags_text = "\n".join(f"- {tag}" for tag in all_tag_names)
    assigned_tags = ", ".join(vocabulary_tag_names) if vocabulary_tag_names else "（なし）"
    description = vocabulary.get("description") or "（なし）"

    return (
        user_template
        .replace("{{VOCABULARY_NAME}}", vocabulary["name"])
        .replace("{{VOCABULARY_DESCRIPTION}}", description)
        .replace("{{VOCABULARY_TAGS}}", assigned_tags)
        .replace("{{ALL_VOCABULARY_NAMES}}", names_text)
        .replace("{{ALL_TAG_NAMES}}", tags_text)
    )


def call_bedrock_check(
    bedrock,
    model_id: str,
    system_prompt: str,
    user_prompt: str,
) -> Dict[str, Any]:
    """Bedrock Converse APIを呼び出してチェック結果JSONを取得する"""
    last_exc: Exception = RuntimeError("Bedrock check failed")
    for attempt in range(1, BEDROCK_MAX_ATTEMPTS + 1):
        try:
            response = bedrock.converse(
                modelId=model_id,
                system=[{"text": system_prompt}],
                messages=[{"role": "user", "content": [{"text": user_prompt}]}],
            )
            raw_json = response["output"]["message"]["content"][0]["text"]
            logger.debug("Bedrock raw response: %s", raw_json)
            return json.loads(raw_json)
        except (BotoCoreError, ClientError) as e:
            last_exc = e
            logger.warning("Bedrock attempt %d/%d failed: %s", attempt, BEDROCK_MAX_ATTEMPTS, e)
            if attempt < BEDROCK_MAX_ATTEMPTS:
                time.sleep(2 ** (attempt - 1))
        except json.JSONDecodeError as e:
            last_exc = e
            logger.warning("Failed to parse Bedrock response as JSON on attempt %d: %s", attempt, e)
            if attempt < BEDROCK_MAX_ATTEMPTS:
                time.sleep(2 ** (attempt - 1))

    raise RuntimeError("Bedrock check failed after max attempts") from last_exc


def save_check_result(
    dynamodb,
    check_results_table_name: str,
    user_id: str,
    vocabulary: Dict[str, Any],
    check_response: Dict[str, Any],
) -> None:
    """チェック結果をvocabulary_check_resultsテーブルに保存する"""
    table = dynamodb.Table(check_results_table_name)
    vocabulary_id = vocabulary["vocabularyId"]
    checked_at = now_iso()
    has_issues = check_response.get("hasIssues", False)

    item: Dict[str, Any] = {
        "userId": user_id,
        "vocabularyId": vocabulary_id,
        "vocabularyCheckResultId": str(uuid.uuid4()),
        "vocabularyName": vocabulary["name"],
        "status": "UNCHECKED" if has_issues else "CHECKED",
        "vocabularyUpdatedAt": vocabulary["updatedDateTime"],
        "checkedAt": checked_at,
    }

    if has_issues:
        item["severity"] = check_response["severity"]
        item["report"] = check_response.get("report", "")
        logger.info(
            "Issues found: vocabularyId=%s severity=%s", vocabulary_id, item["severity"]
        )
    else:
        logger.info("No issues found: vocabularyId=%s", vocabulary_id)

    table.put_item(Item=item)


def process_user(
    dynamodb,
    bedrock,
    vocabularies_table_name: str,
    vocabulary_tags_table_name: str,
    check_results_table_name: str,
    model_id: str,
    user_id: str,
) -> Dict[str, int]:
    """1ユーザー分のボキャブラリーチェックを実行する"""
    system_prompt, user_template = load_prompts()

    vocabularies = get_vocabularies_for_user(dynamodb, vocabularies_table_name, user_id)
    if not vocabularies:
        logger.info("No vocabularies for userId=%s", user_id)
        return {"checked": 0, "skipped": 0, "errors": 0}

    all_tag_names = get_vocabulary_tags_for_user(dynamodb, vocabulary_tags_table_name, user_id)
    all_vocabulary_names = [v["name"] for v in vocabularies]
    tag_id_to_name: Dict[str, str] = {}

    # タグIDとタグ名のマッピングを構築（タグの適切性チェック用）
    vocab_tags_table = dynamodb.Table(vocabulary_tags_table_name)
    tags_response = vocab_tags_table.query(
        KeyConditionExpression="userId = :uid",
        ExpressionAttributeValues={":uid": user_id},
    )
    for tag_item in tags_response.get("Items", []):
        tag_id_to_name[tag_item["vocabularyTagId"]] = tag_item["vocabularyTag"]

    checked = skipped = errors = 0

    for vocabulary in vocabularies:
        vocabulary_id = vocabulary["vocabularyId"]
        try:
            check_result = get_check_result(dynamodb, check_results_table_name, user_id, vocabulary_id)
            if not needs_check(vocabulary, check_result):
                logger.debug("Skipping vocabularyId=%s (already checked)", vocabulary_id)
                skipped += 1
                continue

            tag_ids: List[str] = vocabulary.get("tagIds") or []
            vocabulary_tag_names = [tag_id_to_name[tid] for tid in tag_ids if tid in tag_id_to_name]

            user_prompt = build_user_prompt(
                user_template,
                vocabulary,
                all_vocabulary_names,
                all_tag_names,
                vocabulary_tag_names,
            )
            check_response = call_bedrock_check(bedrock, model_id, system_prompt, user_prompt)
            save_check_result(
                dynamodb,
                check_results_table_name,
                user_id,
                vocabulary,
                check_response,
            )
            checked += 1
        except Exception:
            logger.exception("Failed to check vocabularyId=%s userId=%s", vocabulary_id, user_id)
            errors += 1

    return {"checked": checked, "skipped": skipped, "errors": errors}


def lambda_handler(event: Dict[str, Any], _: Any) -> Dict[str, Any]:
    region = get_env_or_raise("AWS_REGION")
    users_table_name = get_env_or_raise("USERS_TABLE_NAME")
    vocabularies_table_name = get_env_or_raise("VOCABULARIES_TABLE_NAME")
    vocabulary_tags_table_name = get_env_or_raise("VOCABULARY_TAGS_TABLE_NAME")
    check_results_table_name = get_env_or_raise("VOCABULARY_CHECK_RESULTS_TABLE_NAME")
    model_id = get_env_or_raise("BEDROCK_MODEL_ID")

    session = boto3.Session(region_name=region)
    dynamodb = session.resource("dynamodb")
    bedrock = session.client("bedrock-runtime")

    user_ids = get_all_users(dynamodb, users_table_name)
    logger.info("Found %d users to process", len(user_ids))

    total_checked = total_skipped = total_errors = 0

    for user_id in user_ids:
        logger.info("Processing userId=%s", user_id)
        try:
            result = process_user(
                dynamodb=dynamodb,
                bedrock=bedrock,
                vocabularies_table_name=vocabularies_table_name,
                vocabulary_tags_table_name=vocabulary_tags_table_name,
                check_results_table_name=check_results_table_name,
                model_id=model_id,
                user_id=user_id,
            )
            total_checked += result["checked"]
            total_skipped += result["skipped"]
            total_errors += result["errors"]
        except Exception:
            logger.exception("Failed to process userId=%s", user_id)
            total_errors += 1

    logger.info(
        "Completed. checked=%d skipped=%d errors=%d",
        total_checked,
        total_skipped,
        total_errors,
    )
    return {"checked": total_checked, "skipped": total_skipped, "errors": total_errors}
