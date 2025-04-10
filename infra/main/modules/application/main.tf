resource "aws_servicecatalogappregistry_application" "jibun_dashboard_app" {
  name        = "jibun-dashboard"
  description = "自分ダッシュボード"
}

output "application_tag" {
  value = aws_servicecatalogappregistry_application.jibun_dashboard_app.application_tag
}
