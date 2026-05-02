import logging
import os
from datetime import date, datetime, timezone
from typing import Any, Dict, List

import boto3
from boto3.dynamodb.conditions import Attr

logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
    force=True,
)
logger = logging.getLogger(__name__)


def get_env_or_raise(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"Environment variable {name} is required but not set")
    return value


def today_str() -> str:
    return date.today().isoformat()


def scan_expired_qualifications(table, today: str) -> List[Dict[str, Any]]:
    """expirationDateが今日以前でstatusがexpired以外のレコードをスキャン"""
    items: List[Dict[str, Any]] = []
    kwargs: Dict[str, Any] = {
        "FilterExpression": (
            Attr("expirationDate").exists()
            & Attr("expirationDate").lt(today)
            & Attr("status").ne("expired")
        )
    }

    while True:
        response = table.scan(**kwargs)
        items.extend(response.get("Items", []))
        last_key = response.get("LastEvaluatedKey")
        if not last_key:
            break
        kwargs["ExclusiveStartKey"] = last_key

    return items


def expire_qualification(table, user_id: str, qualification_id: str) -> None:
    """qualificationのstatusをexpiredに更新"""
    logger.info(
        "Expiring qualification: userId=%s qualificationId=%s",
        user_id,
        qualification_id,
    )
    table.update_item(
        Key={"userId": user_id, "qualificationId": qualification_id},
        UpdateExpression="SET #s = :s, updatedAt = :u",
        ExpressionAttributeNames={"#s": "status"},
        ExpressionAttributeValues={
            ":s": "expired",
            ":u": datetime.now(timezone.utc).isoformat(),
        },
    )


def lambda_handler(event, _):
    table_name = get_env_or_raise("QUALIFICATIONS_TABLE_NAME")
    region = get_env_or_raise("AWS_REGION")

    dynamodb = boto3.Session(region_name=region).resource("dynamodb")
    table = dynamodb.Table(table_name)  # type: ignore

    today = today_str()
    logger.info("Scanning for expired qualifications. today=%s", today)

    items = scan_expired_qualifications(table, today)
    logger.info("Found %d expired qualifications", len(items))

    updated = 0
    errors = 0
    for item in items:
        user_id = item["userId"]
        qualification_id = item["qualificationId"]
        try:
            expire_qualification(table, user_id, qualification_id)
            updated += 1
        except Exception:
            logger.exception(
                "Failed to expire qualification: userId=%s qualificationId=%s",
                user_id,
                qualification_id,
            )
            errors += 1

    logger.info("Completed. updated=%d errors=%d", updated, errors)
    return {"updated": updated, "errors": errors}
