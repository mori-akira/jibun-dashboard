import json
import os
import uuid
from datetime import datetime
from unittest.mock import Mock, patch

import pytest
from botocore.exceptions import ClientError

from index import (
    OcrTaskMessage,
    build_s3_key,
    call_openai_ocr,
    download_pdf_from_s3,
    get_boto3_clients,
    get_env_or_raise,
    get_openai_api_key_from_ssm,
    lambda_handler,
    load_prompts,
    now_iso,
    parse_sqs_message,
    process_ocr_task,
    save_salary,
    update_task_status,
)


class TestOcrTaskMessage:
    """OcrTaskMessageのテスト"""

    def test_dataclass_creation(self):
        msg = OcrTaskMessage(
            ocr_task_id="task-123",
            user_id="user-456",
            target_date="2025-11-20",
            file_id="file-789",
        )
        assert msg.ocr_task_id == "task-123"
        assert msg.user_id == "user-456"
        assert msg.target_date == "2025-11-20"
        assert msg.file_id == "file-789"


class TestNowIso:
    """now_iso関数のテスト"""

    def test_returns_iso_format(self):
        result = now_iso()
        # ISO 8601形式であることを確認
        datetime.fromisoformat(result)
        assert "T" in result


class TestParseSqsMessage:
    """parse_sqs_message関数のテスト"""

    def test_parse_valid_message(self):
        body = json.dumps({
            "ocrTaskId": "task-123",
            "userId": "user-456",
            "targetDate": "2025-11-20",
            "fileId": "file-789",
        })
        msg = parse_sqs_message(body)
        assert msg.ocr_task_id == "task-123"
        assert msg.user_id == "user-456"
        assert msg.target_date == "2025-11-20"
        assert msg.file_id == "file-789"

    def test_parse_invalid_json(self):
        with pytest.raises(json.JSONDecodeError):
            parse_sqs_message("invalid json")

    def test_parse_missing_field(self):
        body = json.dumps({"ocrTaskId": "task-123"})
        with pytest.raises(KeyError):
            parse_sqs_message(body)


class TestBuildS3Key:
    """build_s3_key関数のテスト"""

    def test_builds_correct_key(self):
        result = build_s3_key("user-123", "file-456")
        assert result == "user-123/file-456.pdf"


class TestGetEnvOrRaise:
    """get_env_or_raise関数のテスト"""

    def test_returns_value_when_set(self):
        with patch.dict(os.environ, {"TEST_VAR": "test_value"}):
            result = get_env_or_raise("TEST_VAR")
            assert result == "test_value"

    def test_raises_when_not_set(self):
        with patch.dict(os.environ, {}, clear=True):
            with pytest.raises(RuntimeError, match="Environment variable TEST_VAR"):
                get_env_or_raise("TEST_VAR")

    def test_raises_when_empty_string(self):
        with patch.dict(os.environ, {"TEST_VAR": ""}):
            with pytest.raises(RuntimeError):
                get_env_or_raise("TEST_VAR")


class TestGetBoto3Clients:
    """get_boto3_clients関数のテスト"""

    @patch("index.boto3.Session")
    def test_returns_all_clients(self, mock_session):
        with patch.dict(os.environ, {"AWS_REGION": "us-east-1"}):
            mock_session_instance = Mock()
            mock_session.return_value = mock_session_instance

            clients = get_boto3_clients()

            assert "sqs" in clients
            assert "s3" in clients
            assert "ssm" in clients
            assert "dynamodb" in clients
            mock_session.assert_called_once_with(region_name="us-east-1")


class TestUpdateTaskStatus:
    """update_task_status関数のテスト"""

    def test_updates_status_successfully(self):
        mock_dynamodb = Mock()
        mock_table = Mock()
        mock_dynamodb.Table.return_value = mock_table

        update_task_status(mock_dynamodb, "test-table", "task-123", "RUNNING")

        mock_dynamodb.Table.assert_called_once_with("test-table")
        mock_table.update_item.assert_called_once()
        call_args = mock_table.update_item.call_args
        assert call_args[1]["Key"] == {"taskId": "task-123"}


class TestDownloadPdfFromS3:
    """download_pdf_from_s3関数のテスト"""

    def test_downloads_successfully(self):
        mock_s3 = Mock()
        pdf_content = b"PDF content"
        mock_body = Mock()
        mock_body.read.return_value = pdf_content
        mock_s3.get_object.return_value = {"Body": mock_body}

        result = download_pdf_from_s3(mock_s3, "test-bucket", "test-key")

        assert result == pdf_content
        mock_s3.get_object.assert_called_once_with(Bucket="test-bucket", Key="test-key")

    def test_raises_on_client_error(self):
        mock_s3 = Mock()
        mock_s3.get_object.side_effect = ClientError(
            {"Error": {"Code": "NoSuchKey"}}, "GetObject"
        )

        with pytest.raises(RuntimeError, match="Failed to download file from S3"):
            download_pdf_from_s3(mock_s3, "test-bucket", "test-key")


class TestGetOpenaiApiKeyFromSsm:
    """get_openai_api_key_from_ssm関数のテスト"""

    def test_fetches_key_successfully(self):
        mock_ssm = Mock()
        mock_ssm.get_parameter.return_value = {
            "Parameter": {"Value": "sk-test-key"}
        }

        result = get_openai_api_key_from_ssm(mock_ssm, "/test/param")

        assert result == "sk-test-key"
        mock_ssm.get_parameter.assert_called_once_with(
            Name="/test/param",
            WithDecryption=True,
        )

    def test_raises_on_client_error(self):
        mock_ssm = Mock()
        mock_ssm.get_parameter.side_effect = ClientError(
            {"Error": {"Code": "ParameterNotFound"}}, "GetParameter"
        )

        with pytest.raises(RuntimeError, match="Failed to fetch OpenAI API key"):
            get_openai_api_key_from_ssm(mock_ssm, "/test/param")


class TestLoadPrompts:
    """load_prompts関数のテスト"""

    def test_loads_and_replaces_placeholder(self, tmp_path):
        # 一時ディレクトリにテストファイルを作成
        prompts_dir = tmp_path / "prompts"
        prompts_dir.mkdir()

        system_file = prompts_dir / "system.md"
        system_file.write_text("System prompt content", encoding="utf-8")

        user_file = prompts_dir / "user.md"
        user_file.write_text("User prompt with {{ONE_SHOT_JSON}} placeholder", encoding="utf-8")

        sample_file = prompts_dir / "sample.json"
        sample_file.write_text('{"test": "value"}', encoding="utf-8")

        with patch("index.PROMPTS_DIR", prompts_dir):
            system_prompt, user_prompt = load_prompts()

        assert system_prompt == "System prompt content"
        assert user_prompt == 'User prompt with {"test": "value"} placeholder'

    def test_raises_on_missing_file(self, tmp_path):
        prompts_dir = tmp_path / "prompts"
        prompts_dir.mkdir()

        with patch("index.PROMPTS_DIR", prompts_dir):
            with pytest.raises(RuntimeError, match="Failed to load prompt files"):
                load_prompts()


class TestCallOpenaiOcr:
    """call_openai_ocr関数のテスト"""

    @patch("index.load_prompts")
    @patch("index.OpenAI")
    def test_calls_openai_api(self, mock_openai_class, mock_load_prompts):
        mock_load_prompts.return_value = ("system prompt", "user prompt")

        mock_client = Mock()
        mock_openai_class.return_value = mock_client

        mock_file_obj = Mock()
        mock_file_obj.id = "file-123"
        mock_client.files.create.return_value = mock_file_obj

        mock_response = Mock()
        mock_response.output_text = '{"overview": {"grossIncome": 400000}}'
        mock_client.responses.create.return_value = mock_response

        with patch.dict(os.environ, {"OPENAI_OCR_MAX_ATTEMPTS": "3"}):
            result = call_openai_ocr(b"pdf content", "sk-test-key", "gpt-5.1")

        assert result == {"overview": {"grossIncome": 400000}}
        mock_client.files.create.assert_called_once()

        # responses.createの呼び出しを詳細に検証
        mock_client.responses.create.assert_called_once()
        call_args = mock_client.responses.create.call_args
        assert call_args[1]["model"] == "gpt-5.1"
        assert "text" in call_args[1]
        assert call_args[1]["text"] == {"format": {"type": "json_object"}}


class TestSaveSalary:
    """save_salary関数のテスト"""

    @patch("index.uuid.uuid4")
    def test_saves_salary_successfully(self, mock_uuid):
        mock_uuid.return_value = uuid.UUID("12345678-1234-5678-1234-567812345678")

        mock_dynamodb = Mock()
        mock_table = Mock()
        mock_dynamodb.Table.return_value = mock_table

        ocr_result = {
            "overview": {"grossIncome": 400000},
            "structure": {"basicSalary": 300000},
            "payslipData": [],
        }

        salary_id = save_salary(
            mock_dynamodb,
            "salaries-table",
            "user-123",
            "2025-11-20",
            ocr_result,
        )

        assert salary_id == "12345678-1234-5678-1234-567812345678"
        mock_table.put_item.assert_called_once()
        call_args = mock_table.put_item.call_args
        item = call_args[1]["Item"]
        assert item["userId"] == "user-123"
        assert item["targetDate"] == "2025-11-20"
        assert item["overview"] == {"grossIncome": 400000}


class TestProcessOcrTask:
    """process_ocr_task関数のテスト"""

    @patch("index.get_boto3_clients")
    @patch("index.get_env_or_raise")
    @patch("index.update_task_status")
    @patch("index.download_pdf_from_s3")
    @patch("index.get_openai_api_key_from_ssm")
    @patch("index.call_openai_ocr")
    @patch("index.save_salary")
    def test_processes_task_successfully(
        self,
        mock_save_salary,
        mock_call_openai,
        mock_get_api_key,
        mock_download_pdf,
        mock_update_status,
        mock_get_env,
        mock_get_clients,
    ):
        mock_get_clients.return_value = {
            "dynamodb": Mock(),
            "s3": Mock(),
            "ssm": Mock(),
        }

        mock_get_env.side_effect = lambda name: {
            "SALARIES_TABLE_NAME": "salaries",
            "SALARY_OCR_TASKS_TABLE_NAME": "tasks",
            "UPLOADS_BUCKET_NAME": "uploads",
            "OPENAI_API_KEY_SSM_NAME": "/openai/key",
            "OPENAI_MODEL": "gpt-5.1",
        }[name]

        mock_download_pdf.return_value = b"pdf content"
        mock_get_api_key.return_value = "sk-test-key"
        mock_call_openai.return_value = {"overview": {}, "structure": {}, "payslipData": []}
        mock_save_salary.return_value = "salary-123"

        msg = OcrTaskMessage(
            ocr_task_id="task-123",
            user_id="user-456",
            target_date="2025-11-20",
            file_id="file-789",
        )

        process_ocr_task(msg)

        # RUNNINGとCOMPLETEDの2回呼ばれることを確認
        assert mock_update_status.call_count == 2

    @patch("index.get_boto3_clients")
    @patch("index.get_env_or_raise")
    @patch("index.update_task_status")
    @patch("index.download_pdf_from_s3")
    def test_updates_to_failed_on_error(
        self,
        mock_download_pdf,
        mock_update_status,
        mock_get_env,
        mock_get_clients,
    ):
        mock_get_clients.return_value = {
            "dynamodb": Mock(),
            "s3": Mock(),
            "ssm": Mock(),
        }

        mock_get_env.side_effect = lambda name: {
            "SALARIES_TABLE_NAME": "salaries",
            "SALARY_OCR_TASKS_TABLE_NAME": "tasks",
            "UPLOADS_BUCKET_NAME": "uploads",
            "OPENAI_API_KEY_SSM_NAME": "/openai/key",
            "OPENAI_MODEL": "gpt-5.1",
        }[name]

        mock_download_pdf.side_effect = RuntimeError("Download failed")

        msg = OcrTaskMessage(
            ocr_task_id="task-123",
            user_id="user-456",
            target_date="2025-11-20",
            file_id="file-789",
        )

        with pytest.raises(RuntimeError):
            process_ocr_task(msg)

        # RUNNINGとFAILEDの2回呼ばれることを確認
        assert mock_update_status.call_count == 2
        failed_call = mock_update_status.call_args_list[1]
        assert failed_call[0][2] == "task-123"
        assert failed_call[0][3] == "FAILED"


class TestLambdaHandler:
    """lambda_handler関数のテスト"""

    @patch("index.process_ocr_task")
    def test_processes_single_record_successfully(self, mock_process_task):
        event = {
            "Records": [
                {
                    "messageId": "msg-123",
                    "body": json.dumps({
                        "ocrTaskId": "task-123",
                        "userId": "user-456",
                        "targetDate": "2025-11-20",
                        "fileId": "file-789",
                    }),
                }
            ]
        }

        result = lambda_handler(event, None)

        mock_process_task.assert_called_once()
        assert result == {"batchItemFailures": []}

    @patch("index.process_ocr_task")
    def test_processes_multiple_records(self, mock_process_task):
        event = {
            "Records": [
                {
                    "messageId": "msg-123",
                    "body": json.dumps({
                        "ocrTaskId": "task-123",
                        "userId": "user-456",
                        "targetDate": "2025-11-20",
                        "fileId": "file-789",
                    }),
                },
                {
                    "messageId": "msg-456",
                    "body": json.dumps({
                        "ocrTaskId": "task-456",
                        "userId": "user-789",
                        "targetDate": "2025-11-21",
                        "fileId": "file-012",
                    }),
                },
            ]
        }

        result = lambda_handler(event, None)

        assert mock_process_task.call_count == 2
        assert result == {"batchItemFailures": []}

    def test_returns_empty_failures_when_no_records(self):
        event = {}
        result = lambda_handler(event, None)
        assert result == {"batchItemFailures": []}

    @patch("index.process_ocr_task")
    def test_returns_failed_message_on_error(self, mock_process_task):
        mock_process_task.side_effect = RuntimeError("Processing error")

        event = {
            "Records": [
                {
                    "messageId": "msg-123",
                    "body": json.dumps({
                        "ocrTaskId": "task-123",
                        "userId": "user-456",
                        "targetDate": "2025-11-20",
                        "fileId": "file-789",
                    }),
                }
            ]
        }

        result = lambda_handler(event, None)

        assert result == {"batchItemFailures": [{"itemIdentifier": "msg-123"}]}

    @patch("index.process_ocr_task")
    def test_returns_only_failed_messages(self, mock_process_task):
        # 1つ目は成功、2つ目は失敗
        mock_process_task.side_effect = [None, RuntimeError("Processing error")]

        event = {
            "Records": [
                {
                    "messageId": "msg-success",
                    "body": json.dumps({
                        "ocrTaskId": "task-123",
                        "userId": "user-456",
                        "targetDate": "2025-11-20",
                        "fileId": "file-789",
                    }),
                },
                {
                    "messageId": "msg-fail",
                    "body": json.dumps({
                        "ocrTaskId": "task-456",
                        "userId": "user-789",
                        "targetDate": "2025-11-21",
                        "fileId": "file-012",
                    }),
                },
            ]
        }

        result = lambda_handler(event, None)

        assert result == {"batchItemFailures": [{"itemIdentifier": "msg-fail"}]}
        assert mock_process_task.call_count == 2
