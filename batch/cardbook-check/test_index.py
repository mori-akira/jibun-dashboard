import json
import os
from datetime import datetime
from unittest.mock import MagicMock, patch

import pytest

from index import (
    build_user_prompt,
    call_bedrock_check,
    card_updated_at,
    get_all_users,
    get_cards_for_user,
    get_check_result,
    get_env_or_raise,
    lambda_handler,
    needs_check,
    now_iso,
    save_check_result,
)


class TestGetEnvOrRaise:
    def test_returns_value_when_set(self):
        with patch.dict(os.environ, {"TEST_VAR": "value"}):
            assert get_env_or_raise("TEST_VAR") == "value"

    def test_raises_when_not_set(self):
        with patch.dict(os.environ, {}, clear=True):
            with pytest.raises(RuntimeError, match="TEST_VAR"):
                get_env_or_raise("TEST_VAR")


class TestNowIso:
    def test_returns_utc_iso_string(self):
        dt = datetime.fromisoformat(now_iso())
        assert dt.tzinfo is not None


class TestGetAllUsers:
    def test_handles_pagination(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.scan.side_effect = [
            {"Items": [{"userId": "u1"}], "LastEvaluatedKey": {"userId": "u1"}},
            {"Items": [{"userId": "u2"}]},
        ]

        result = get_all_users(mock_dynamodb, "users")

        assert result == ["u1", "u2"]
        assert mock_table.scan.call_count == 2


class TestGetCardsForUser:
    def test_handles_pagination(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.query.side_effect = [
            {"Items": [{"cardId": "c1"}], "LastEvaluatedKey": {"userId": "u1", "cardId": "c1"}},
            {"Items": [{"cardId": "c2"}]},
        ]

        result = get_cards_for_user(mock_dynamodb, "cardbook_cards", "u1")

        assert len(result) == 2
        assert mock_table.query.call_count == 2


class TestGetCheckResult:
    def test_returns_item_when_found(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        item = {"userId": "u1", "cardId": "c1", "severity": "HIGH"}
        mock_table.get_item.return_value = {"Item": item}

        assert get_check_result(mock_dynamodb, "check_results", "u1", "c1") == item

    def test_returns_none_when_not_found(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.get_item.return_value = {}

        assert get_check_result(mock_dynamodb, "check_results", "u1", "c1") is None


class TestCardUpdatedAt:
    def test_uses_updated_date_time_when_present(self):
        card = {"updatedDateTime": "2025-06-01T00:00:00+00:00", "createdDateTime": "2025-01-01T00:00:00+00:00"}
        assert card_updated_at(card) == "2025-06-01T00:00:00+00:00"

    def test_falls_back_to_created_date_time_for_legacy_card(self):
        card = {"createdDateTime": "2025-01-01T00:00:00+00:00"}
        assert card_updated_at(card) == "2025-01-01T00:00:00+00:00"


class TestNeedsCheck:
    def test_returns_true_when_no_check_result(self):
        card = {"updatedDateTime": "2025-01-01T00:00:00+00:00", "createdDateTime": "2025-01-01T00:00:00+00:00"}
        assert needs_check(card, None) is True

    def test_returns_true_when_card_updated_after_check(self):
        card = {"updatedDateTime": "2025-06-01T00:00:00+00:00", "createdDateTime": "2025-01-01T00:00:00+00:00"}
        check_result = {"checkedAt": "2025-01-01T00:00:00+00:00"}
        assert needs_check(card, check_result) is True

    def test_returns_false_when_card_not_updated_since_check(self):
        card = {"updatedDateTime": "2025-01-01T00:00:00+00:00", "createdDateTime": "2025-01-01T00:00:00+00:00"}
        check_result = {"checkedAt": "2025-06-01T00:00:00+00:00"}
        assert needs_check(card, check_result) is False

    def test_legacy_card_uses_created_date_time(self):
        card = {"createdDateTime": "2025-06-01T00:00:00+00:00"}
        check_result = {"checkedAt": "2025-01-01T00:00:00+00:00"}
        assert needs_check(card, check_result) is True


class TestBuildUserPrompt:
    TEMPLATE = (
        "Front: {{CARD_FRONT}}\n"
        "Back: {{CARD_BACK}}\n"
        "Fronts: {{CARDBOOK_CARD_FRONTS}}"
    )

    def test_substitutes_all_placeholders(self):
        card = {"front": "ephemeral", "back": "儚い"}
        result = build_user_prompt(self.TEMPLATE, card, ["ephemeral", "candid"])
        assert "ephemeral" in result
        assert "儚い" in result
        assert "candid" in result

    def test_uses_placeholder_when_no_back(self):
        card = {"front": "ephemeral", "back": None}
        result = build_user_prompt(self.TEMPLATE, card, [])
        assert "（なし）" in result


class TestCallBedrockCheck:
    def _mock_bedrock(self, response_text):
        mock_bedrock = MagicMock()
        mock_bedrock.converse.return_value = {
            "output": {"message": {"content": [{"text": response_text}]}}
        }
        return mock_bedrock

    def test_returns_parsed_json(self):
        payload = {"hasIssues": False, "severity": None, "issues": [], "report": ""}
        bedrock = self._mock_bedrock(json.dumps(payload))

        assert call_bedrock_check(bedrock, "model-id", "system", "user") == payload

    def test_retries_on_json_parse_error(self):
        good = json.dumps({"hasIssues": False, "severity": None, "issues": [], "report": ""})
        bedrock = MagicMock()
        bedrock.converse.side_effect = [
            {"output": {"message": {"content": [{"text": "not-json"}]}}},
            {"output": {"message": {"content": [{"text": good}]}}},
        ]

        with patch("index.time.sleep"):
            result = call_bedrock_check(bedrock, "model-id", "system", "user")

        assert result["hasIssues"] is False
        assert bedrock.converse.call_count == 2

    def test_raises_after_max_attempts(self):
        bedrock = self._mock_bedrock("invalid-json")

        with patch("index.time.sleep"):
            with pytest.raises(RuntimeError, match="Bedrock check failed after max attempts"):
                call_bedrock_check(bedrock, "model-id", "system", "user")


class TestSaveCheckResult:
    def _card(self):
        return {
            "cardId": "c1",
            "cardbookId": "cb1",
            "front": "ephemeral",
            "back": "儚い",
            "createdDateTime": "2025-01-01T00:00:00+00:00",
            "updatedDateTime": "2025-02-01T00:00:00+00:00",
        }

    def test_saves_severity_and_report_when_has_issues(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        check_response = {"hasIssues": True, "severity": "HIGH", "report": "## issue"}

        save_check_result(mock_dynamodb, "check_results", "u1", self._card(), check_response)

        item = mock_table.put_item.call_args[1]["Item"]
        assert item["cardId"] == "c1"
        assert item["cardbookId"] == "cb1"
        assert item["front"] == "ephemeral"
        assert item["severity"] == "HIGH"
        assert item["report"] == "## issue"
        assert item["status"] == "UNCHECKED"
        assert item["cardUpdatedAt"] == "2025-02-01T00:00:00+00:00"

    def test_omits_severity_when_no_issues(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        check_response = {"hasIssues": False, "severity": None, "issues": [], "report": ""}

        save_check_result(mock_dynamodb, "check_results", "u1", self._card(), check_response)

        item = mock_table.put_item.call_args[1]["Item"]
        assert "severity" not in item
        assert item["status"] == "CHECKED"


class TestLambdaHandler:
    ENV = {
        "AWS_REGION": "ap-northeast-1",
        "USERS_TABLE_NAME": "users",
        "CARDBOOK_CARDS_TABLE_NAME": "cardbook-cards",
        "CARDBOOK_CHECK_RESULTS_TABLE_NAME": "cardbook-check-results",
        "BEDROCK_MODEL_ID": "anthropic.claude-3-haiku",
    }

    def _setup_tables(self, mock_session, *, users, cards, check_result):
        mock_dynamodb = MagicMock()
        mock_session.return_value.resource.return_value = mock_dynamodb

        users_table = MagicMock()
        users_table.scan.return_value = {"Items": [{"userId": u} for u in users]}

        cards_table = MagicMock()
        cards_table.query.return_value = {"Items": cards}

        check_table = MagicMock()
        check_table.get_item.return_value = {"Item": check_result} if check_result else {}

        def table_factory(name):
            if "users" in name:
                return users_table
            if "check" in name:
                return check_table
            if "cards" in name:
                return cards_table
            return MagicMock()

        mock_dynamodb.Table.side_effect = table_factory
        return mock_dynamodb

    def _card(self, **overrides):
        card = {
            "cardId": "c1",
            "cardbookId": "cb1",
            "front": "ephemeral",
            "back": "儚い",
            "createdDateTime": "2025-01-01T00:00:00+00:00",
            "updatedDateTime": "2025-06-01T00:00:00+00:00",
        }
        card.update(overrides)
        return card

    @patch("index.boto3.Session")
    def test_returns_totals(self, mock_session):
        self._setup_tables(mock_session, users=["u1"], cards=[self._card()], check_result=None)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock
        mock_bedrock.converse.return_value = {
            "output": {
                "message": {
                    "content": [
                        {"text": json.dumps({"hasIssues": False, "severity": None, "issues": [], "report": ""})}
                    ]
                }
            }
        }

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result["checked"] == 1
        assert result["skipped"] == 0
        assert result["errors"] == 0

    @patch("index.boto3.Session")
    def test_skips_card_without_back(self, mock_session):
        self._setup_tables(mock_session, users=["u1"], cards=[self._card(back="")], check_result=None)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result["skipped"] == 1
        mock_bedrock.converse.assert_not_called()

    @patch("index.boto3.Session")
    def test_skips_already_checked_card(self, mock_session):
        card = self._card(updatedDateTime="2025-01-01T00:00:00+00:00")
        existing = {"checkedAt": "2025-06-01T00:00:00+00:00"}
        self._setup_tables(mock_session, users=["u1"], cards=[card], check_result=existing)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result["skipped"] == 1
        mock_bedrock.converse.assert_not_called()

    @patch("index.boto3.Session")
    def test_counts_errors_on_bedrock_failure(self, mock_session):
        self._setup_tables(mock_session, users=["u1"], cards=[self._card()], check_result=None)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock
        mock_bedrock.converse.return_value = {
            "output": {"message": {"content": [{"text": "not-json"}]}}
        }

        with patch.dict(os.environ, self.ENV), patch("index.time.sleep"):
            result = lambda_handler({}, None)

        assert result["errors"] == 1
