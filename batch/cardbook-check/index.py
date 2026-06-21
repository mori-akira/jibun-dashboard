import json
import logging
import os
import re
import time
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

import boto3
from botocore.exceptions import BotoCoreError, ClientError

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
    force=True,
)
logger = logging.getLogger(__name__)

BASE_DIR = Path(__file__).resolve().parent
PROMPTS_DIR = BASE_DIR / "prompts"
BEDROCK_MAX_ATTEMPTS = 3
_CODE_FENCE_RE = re.compile(r"^```(?:json)?\s*\n(.*)\n```\s*$", re.DOTALL)


def strip_code_fence(text: str) -> str:
    m = _CODE_FENCE_RE.match(text.strip())
    return m.group(1) if m else text


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


def get_cards_for_user(dynamodb, cardbook_cards_table_name: str, user_id: str) -> List[Dict[str, Any]]:
    """ユーザーのカード全件を取得"""
    table = dynamodb.Table(cardbook_cards_table_name)
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


def get_check_result(
    dynamodb,
    check_results_table_name: str,
    user_id: str,
    card_id: str,
) -> Optional[Dict[str, Any]]:
    """チェック結果テーブルから既存レコードを取得"""
    table = dynamodb.Table(check_results_table_name)
    response = table.get_item(Key={"userId": user_id, "cardId": card_id})
    return response.get("Item")


def card_updated_at(card: Dict[str, Any]) -> str:
    """カードの更新日時を返す。updatedDateTime を持たない既存カードは createdDateTime にフォールバックする"""
    return card.get("updatedDateTime") or card["createdDateTime"]


def needs_check(card: Dict[str, Any], check_result: Optional[Dict[str, Any]]) -> bool:
    """チェックが必要かどうかを判定する"""
    if check_result is None:
        return True
    checked_at = datetime.fromisoformat(check_result["checkedAt"])
    updated_at = datetime.fromisoformat(card_updated_at(card))
    return updated_at > checked_at


def build_user_prompt(
    user_template: str,
    card: Dict[str, Any],
    cardbook_card_fronts: List[str],
) -> str:
    """ユーザープロンプトを構築する"""
    fronts_text = "\n".join(f"- {front}" for front in cardbook_card_fronts)
    back = card.get("back") or "（なし）"

    return (
        user_template
        .replace("{{CARD_FRONT}}", card["front"])
        .replace("{{CARD_BACK}}", back)
        .replace("{{CARDBOOK_CARD_FRONTS}}", fronts_text)
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
            return json.loads(strip_code_fence(raw_json))
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
    card: Dict[str, Any],
    check_response: Dict[str, Any],
) -> None:
    """チェック結果をcardbook_check_resultsテーブルに保存する"""
    table = dynamodb.Table(check_results_table_name)
    card_id = card["cardId"]
    checked_at = now_iso()
    has_issues = check_response.get("hasIssues", False)

    item: Dict[str, Any] = {
        "userId": user_id,
        "cardId": card_id,
        "cardbookCheckResultId": str(uuid.uuid4()),
        "cardbookId": card["cardbookId"],
        "front": card["front"],
        "status": "UNCHECKED" if has_issues else "CHECKED",
        "cardUpdatedAt": card_updated_at(card),
        "checkedAt": checked_at,
    }

    if has_issues:
        item["severity"] = check_response["severity"]
        item["report"] = check_response.get("report", "")
        logger.info("Issues found: cardId=%s severity=%s", card_id, item["severity"])
    else:
        logger.info("No issues found: cardId=%s", card_id)

    table.put_item(Item=item)


def process_user(
    dynamodb,
    bedrock,
    cardbook_cards_table_name: str,
    check_results_table_name: str,
    model_id: str,
    user_id: str,
) -> Dict[str, int]:
    """1ユーザー分のカードチェックを実行する"""
    system_prompt, user_template = load_prompts()

    cards = get_cards_for_user(dynamodb, cardbook_cards_table_name, user_id)
    if not cards:
        logger.info("No cards for userId=%s", user_id)
        return {"checked": 0, "skipped": 0, "errors": 0}

    # 重複検知用に、カード帳ごとの表（front）一覧を構築する
    fronts_by_cardbook: Dict[str, List[str]] = {}
    for card in cards:
        fronts_by_cardbook.setdefault(card["cardbookId"], []).append(card["front"])

    checked = skipped = errors = 0

    for card in cards:
        card_id = card["cardId"]
        if not card.get("back"):
            logger.debug("Skipping cardId=%s (no back)", card_id)
            skipped += 1
            continue
        try:
            check_result = get_check_result(dynamodb, check_results_table_name, user_id, card_id)
            if not needs_check(card, check_result):
                logger.debug("Skipping cardId=%s (already checked)", card_id)
                skipped += 1
                continue

            cardbook_card_fronts = fronts_by_cardbook.get(card["cardbookId"], [])

            user_prompt = build_user_prompt(user_template, card, cardbook_card_fronts)
            check_response = call_bedrock_check(bedrock, model_id, system_prompt, user_prompt)
            save_check_result(
                dynamodb,
                check_results_table_name,
                user_id,
                card,
                check_response,
            )
            checked += 1
        except Exception:
            logger.exception("Failed to check cardId=%s userId=%s", card_id, user_id)
            errors += 1

    return {"checked": checked, "skipped": skipped, "errors": errors}


def lambda_handler(event: Dict[str, Any], _: Any) -> Dict[str, Any]:
    region = get_env_or_raise("AWS_REGION")
    users_table_name = get_env_or_raise("USERS_TABLE_NAME")
    cardbook_cards_table_name = get_env_or_raise("CARDBOOK_CARDS_TABLE_NAME")
    check_results_table_name = get_env_or_raise("CARDBOOK_CHECK_RESULTS_TABLE_NAME")
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
                cardbook_cards_table_name=cardbook_cards_table_name,
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
