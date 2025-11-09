resource "aws_ssm_parameter" "openai_api_key" {
  name        = "/${var.app_name}/${var.env_name}/backend/openai/api_key"
  description = "OpenAIのAPIキー"
  type        = "SecureString"
  value       = var.openai_api_key
  tags        = var.application_tag
}
