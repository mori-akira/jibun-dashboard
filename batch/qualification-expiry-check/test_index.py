import os
from datetime import date
from unittest.mock import MagicMock, patch

import pytest

from index import (
    expire_qualification,
    get_env_or_raise,
    lambda_handler,
    scan_expired_qualifications,
    today_str,
)


class TestTodayStr:
    def test_returns_iso_date(self):
        date.fromisoformat(today_str())


class TestGetEnvOrRaise:
    def test_returns_value_when_set(self):
        with patch.dict(os.environ, {"TEST_VAR": "test_value"}):
            assert get_env_or_raise("TEST_VAR") == "test_value"

    def test_raises_when_not_set(self):
        with patch.dict(os.environ, {}, clear=True):
            with pytest.raises(RuntimeError, match="Environment variable TEST_VAR"):
                get_env_or_raise("TEST_VAR")


class TestScanExpiredQualifications:
    def test_handles_pagination(self):
        mock_table = MagicMock()
        mock_table.scan.side_effect = [
            {
                "Items": [{"userId": "user-1", "qualificationId": "q-1"}],
                "LastEvaluatedKey": {"userId": "user-1", "qualificationId": "q-1"},
            },
            {
                "Items": [{"userId": "user-2", "qualificationId": "q-2"}],
            },
        ]

        result = scan_expired_qualifications(mock_table, "2026-04-19")

        assert len(result) == 2
        assert mock_table.scan.call_count == 2
        assert "ExclusiveStartKey" in mock_table.scan.call_args_list[1][1]


class TestExpireQualification:
    def test_updates_status_to_expired(self):
        mock_table = MagicMock()

        expire_qualification(mock_table, "user-1", "q-1")

        call_kwargs = mock_table.update_item.call_args[1]
        assert call_kwargs["Key"] == {"userId": "user-1", "qualificationId": "q-1"}
        assert call_kwargs["ExpressionAttributeValues"][":s"] == "expired"


class TestLambdaHandler:
    ENV = {"QUALIFICATIONS_TABLE_NAME": "qualifications", "AWS_REGION": "ap-northeast-1"}

    @patch("index.boto3.Session")
    def test_continues_on_partial_failure(self, mock_session):
        mock_table = MagicMock()
        mock_session.return_value.resource.return_value.Table.return_value = mock_table
        mock_table.scan.return_value = {
            "Items": [
                {"userId": "user-1", "qualificationId": "q-1", "expirationDate": "2024-01-01", "status": "acquired"},
                {"userId": "user-2", "qualificationId": "q-2", "expirationDate": "2024-01-01", "status": "acquired"},
            ]
        }
        mock_table.update_item.side_effect = [None, Exception("DynamoDB error")]

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result == {"updated": 1, "errors": 1}

    @patch("index.boto3.Session")
    def test_returns_zero_when_no_expired(self, mock_session):
        mock_table = MagicMock()
        mock_session.return_value.resource.return_value.Table.return_value = mock_table
        mock_table.scan.return_value = {"Items": []}

        with patch.dict(os.environ, self.ENV):
            result = lambda_handler({}, None)

        assert result == {"updated": 0, "errors": 0}
        mock_table.update_item.assert_not_called()
