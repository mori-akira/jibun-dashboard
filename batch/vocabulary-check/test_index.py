import json
import os
from datetime import datetime
from unittest.mock import MagicMock, patch

import pytest

from index import (
    build_user_prompt,
    call_bedrock_check,
    get_all_users,
    get_check_result,
    get_env_or_raise,
    get_vocabularies_for_user,
    get_vocabulary_tags_for_user,
    needs_check,
    now_iso,
    save_check_result,
    lambda_handler,
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
        result = now_iso()
        dt = datetime.fromisoformat(result)
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
        assert "ExclusiveStartKey" in mock_table.scan.call_args_list[1][1]

    def test_returns_empty_list_when_no_users(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.scan.return_value = {"Items": []}

        result = get_all_users(mock_dynamodb, "users")

        assert result == []


class TestGetVocabulariesForUser:
    def test_handles_pagination(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        vocab1 = {"vocabularyId": "v1", "name": "API"}
        vocab2 = {"vocabularyId": "v2", "name": "SDK"}
        mock_table.query.side_effect = [
            {"Items": [vocab1], "LastEvaluatedKey": {"userId": "u1", "vocabularyId": "v1"}},
            {"Items": [vocab2]},
        ]

        result = get_vocabularies_for_user(mock_dynamodb, "vocabularies", "u1")

        assert len(result) == 2
        assert mock_table.query.call_count == 2


class TestGetVocabularyTagsForUser:
    def test_returns_tag_names(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.query.return_value = {
            "Items": [{"vocabularyTag": "IT"}, {"vocabularyTag": "Business"}]
        }

        result = get_vocabulary_tags_for_user(mock_dynamodb, "vocab_tags", "u1")

        assert result == ["IT", "Business"]


class TestGetCheckResult:
    def test_returns_item_when_found(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        item = {"userId": "u1", "vocabularyId": "v1", "severity": "HIGH"}
        mock_table.get_item.return_value = {"Item": item}

        result = get_check_result(mock_dynamodb, "check_results", "u1", "v1")

        assert result == item

    def test_returns_none_when_not_found(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        mock_table.get_item.return_value = {}

        result = get_check_result(mock_dynamodb, "check_results", "u1", "v1")

        assert result is None


class TestNeedsCheck:
    def test_returns_true_when_no_check_result(self):
        vocab = {"updatedDateTime": "2025-01-01T00:00:00+00:00"}
        assert needs_check(vocab, None) is True

    def test_returns_true_when_vocabulary_updated_after_check(self):
        vocab = {"updatedDateTime": "2025-06-01T00:00:00+00:00"}
        check_result = {"checkedAt": "2025-01-01T00:00:00+00:00"}
        assert needs_check(vocab, check_result) is True

    def test_returns_false_when_vocabulary_not_updated_since_check(self):
        vocab = {"updatedDateTime": "2025-01-01T00:00:00+00:00"}
        check_result = {"checkedAt": "2025-06-01T00:00:00+00:00"}
        assert needs_check(vocab, check_result) is False


class TestBuildUserPrompt:
    TEMPLATE = (
        "Name: {{VOCABULARY_NAME}}\n"
        "Desc: {{VOCABULARY_DESCRIPTION}}\n"
        "Tags: {{VOCABULARY_TAGS}}\n"
        "All: {{ALL_VOCABULARY_NAMES}}\n"
        "AllTags: {{ALL_TAG_NAMES}}"
    )

    def test_substitutes_all_placeholders(self):
        vocab = {"name": "API", "description": "Application interface"}
        result = build_user_prompt(self.TEMPLATE, vocab, ["API", "SDK"], ["IT", "Business"], ["IT"])
        assert "API" in result
        assert "Application interface" in result
        assert "IT" in result
        assert "SDK" in result
        assert "Business" in result

    def test_uses_placeholder_when_no_description(self):
        vocab = {"name": "API", "description": None}
        result = build_user_prompt(self.TEMPLATE, vocab, [], [], [])
        assert "（なし）" in result

    def test_uses_placeholder_when_no_tags(self):
        vocab = {"name": "API", "description": "desc"}
        result = build_user_prompt(self.TEMPLATE, vocab, [], [], [])
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

        result = call_bedrock_check(bedrock, "model-id", "system", "user")

        assert result == payload

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
        bedrock = MagicMock()
        bedrock.converse.return_value = {
            "output": {"message": {"content": [{"text": "invalid-json"}]}}
        }

        with patch("index.time.sleep"):
            with pytest.raises(RuntimeError, match="Bedrock check failed after max attempts"):
                call_bedrock_check(bedrock, "model-id", "system", "user")


class TestSaveCheckResult:
    def _vocab(self):
        return {
            "vocabularyId": "v1",
            "name": "API",
            "updatedDateTime": "2025-01-01T00:00:00+00:00",
        }

    def test_saves_severity_and_report_when_has_issues(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        check_response = {"hasIssues": True, "severity": "HIGH", "report": "## issue"}

        save_check_result(mock_dynamodb, "check_results", "u1", self._vocab(), check_response)

        item = mock_table.put_item.call_args[1]["Item"]
        assert item["severity"] == "HIGH"
        assert item["report"] == "## issue"
        assert item["status"] == "UNCHECKED"

    def test_omits_severity_when_no_issues(self):
        mock_dynamodb = MagicMock()
        mock_table = MagicMock()
        mock_dynamodb.Table.return_value = mock_table
        check_response = {"hasIssues": False, "severity": None, "issues": [], "report": ""}

        save_check_result(mock_dynamodb, "check_results", "u1", self._vocab(), check_response)

        item = mock_table.put_item.call_args[1]["Item"]
        assert "severity" not in item
        assert item["status"] == "CHECKED"


class TestLambdaHandler:
    ENV = {
        "AWS_REGION": "ap-northeast-1",
        "USERS_TABLE_NAME": "users",
        "VOCABULARIES_TABLE_NAME": "vocabularies",
        "VOCABULARY_TAGS_TABLE_NAME": "vocab_tags",
        "VOCABULARY_CHECK_RESULTS_TABLE_NAME": "check_results",
        "BEDROCK_MODEL_ID": "anthropic.claude-3-haiku",
    }

    def _setup_tables(self, mock_session, *, users, vocabularies, tags, check_result, tag_items=None):
        mock_dynamodb = MagicMock()
        mock_session.return_value.resource.return_value = mock_dynamodb

        users_table = MagicMock()
        users_table.scan.return_value = {"Items": [{"userId": u} for u in users]}

        vocab_table = MagicMock()
        vocab_table.query.return_value = {"Items": vocabularies}

        tags_table = MagicMock()
        if tag_items is None:
            tag_items = [{"vocabularyTagId": f"t{i}", "vocabularyTag": t} for i, t in enumerate(tags)]
        tags_table.query.return_value = {"Items": tag_items}

        check_table = MagicMock()
        check_table.get_item.return_value = {"Item": check_result} if check_result else {}

        def table_factory(name):
            if "users" in name and "vocab" not in name:
                return users_table
            if "vocab-tags" in name or "vocab_tags" in name:
                return tags_table
            if "vocabularies" in name:
                return vocab_table
            if "check" in name:
                return check_table
            return MagicMock()

        mock_dynamodb.Table.side_effect = table_factory
        return mock_dynamodb

    @patch("index.boto3.Session")
    def test_returns_totals(self, mock_session):
        vocab = {
            "vocabularyId": "v1",
            "name": "API",
            "description": "desc",
            "updatedDateTime": "2025-06-01T00:00:00+00:00",
            "tagIds": [],
        }
        self._setup_tables(mock_session, users=["u1"], vocabularies=[vocab], tags=[], check_result=None)
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
    def test_skips_already_checked_vocabulary(self, mock_session):
        vocab = {
            "vocabularyId": "v1",
            "name": "API",
            "description": "desc",
            "updatedDateTime": "2025-01-01T00:00:00+00:00",
            "tagIds": [],
        }
        existing = {"checkedAt": "2025-06-01T00:00:00+00:00"}
        self._setup_tables(mock_session, users=["u1"], vocabularies=[vocab], tags=[], check_result=existing)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result["skipped"] == 1
        mock_bedrock.converse.assert_not_called()

    @patch("index.boto3.Session")
    def test_counts_errors_on_bedrock_failure(self, mock_session):
        vocab = {
            "vocabularyId": "v1",
            "name": "API",
            "description": "desc",
            "updatedDateTime": "2025-06-01T00:00:00+00:00",
            "tagIds": [],
        }
        self._setup_tables(mock_session, users=["u1"], vocabularies=[vocab], tags=[], check_result=None)
        mock_bedrock = MagicMock()
        mock_session.return_value.client.return_value = mock_bedrock
        mock_bedrock.converse.return_value = {
            "output": {"message": {"content": [{"text": "not-json"}]}}
        }

        with patch.dict(os.environ, self.ENV), patch("index.time.sleep"):
            result = lambda_handler({}, None)

        assert result["errors"] == 1
