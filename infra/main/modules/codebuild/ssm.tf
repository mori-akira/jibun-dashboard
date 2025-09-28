resource "aws_ssm_parameter" "e2e_password" {
  name        = "/${var.project_name}/e2e/password"
  description = "E2Eテスト用のパスワード"
  type        = "SecureString"
  value       = var.password
  tags        = var.application_tag
}
