import os
from unittest.mock import Mock, patch
from index import handler


class TestHandler:
    """handler関数のテスト"""

    @patch("index.boto3.client")
    def test_pause_service_with_event_action(self, mock_boto_client):
        """eventのactionでpauseを指定した場合"""
        mock_client = Mock()
        mock_boto_client.return_value = mock_client

        with patch.dict(os.environ, {"SERVICE_ARN": "arn:aws:apprunner:us-east-1:123456789012:service/test"}):
            event = {"action": "pause"}
            result = handler(event, None)

        mock_client.pause_service.assert_called_once_with(
            ServiceArn="arn:aws:apprunner:us-east-1:123456789012:service/test"
        )
        assert result == {"status": "paused"}

    @patch("index.boto3.client")
    def test_resume_service_with_event_action(self, mock_boto_client):
        """eventのactionでresumeを指定した場合"""
        mock_client = Mock()
        mock_boto_client.return_value = mock_client

        with patch.dict(os.environ, {"SERVICE_ARN": "arn:aws:apprunner:us-east-1:123456789012:service/test"}):
            event = {"action": "resume"}
            result = handler(event, None)

        mock_client.resume_service.assert_called_once_with(
            ServiceArn="arn:aws:apprunner:us-east-1:123456789012:service/test"
        )
        assert result == {"status": "resumed"}

    @patch("index.boto3.client")
    def test_pause_service_with_env_action(self, mock_boto_client):
        """環境変数のACTIONでpauseを指定した場合"""
        mock_client = Mock()
        mock_boto_client.return_value = mock_client

        with patch.dict(
            os.environ,
            {
                "SERVICE_ARN": "arn:aws:apprunner:us-east-1:123456789012:service/test",
                "ACTION": "pause",
            },
        ):
            event = {}
            result = handler(event, None)

        mock_client.pause_service.assert_called_once_with(
            ServiceArn="arn:aws:apprunner:us-east-1:123456789012:service/test"
        )
        assert result == {"status": "paused"}

    @patch("index.boto3.client")
    def test_resume_service_with_env_action(self, mock_boto_client):
        """環境変数のACTIONでresumeを指定した場合"""
        mock_client = Mock()
        mock_boto_client.return_value = mock_client

        with patch.dict(
            os.environ,
            {
                "SERVICE_ARN": "arn:aws:apprunner:us-east-1:123456789012:service/test",
                "ACTION": "resume",
            },
        ):
            event = {}
            result = handler(event, None)

        mock_client.resume_service.assert_called_once_with(
            ServiceArn="arn:aws:apprunner:us-east-1:123456789012:service/test"
        )
        assert result == {"status": "resumed"}
