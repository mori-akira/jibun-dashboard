resource "aws_iam_role" "events_to_codebuild" {
  name = "${var.project_name}-e2e-events-to-codebuild"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect    = "Allow",
      Principal = { Service = "events.amazonaws.com" },
      Action    = "sts:AssumeRole"
    }]
  })
  tags = var.application_tag
}

resource "aws_iam_role_policy" "events_to_codebuild" {
  role = aws_iam_role.events_to_codebuild.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = ["codebuild:StartBuild"],
      Resource = aws_codebuild_project.this.arn
    }]
  })
}

resource "aws_s3_bucket_notification" "frontend_eventbridge" {
  count       = var.enable_trigger_s3 ? 1 : 0
  bucket      = var.s3_bucket_name
  eventbridge = true
}

resource "aws_cloudwatch_event_rule" "s3_front_updated" {
  count = var.enable_trigger_s3 ? 1 : 0
  name  = "${var.project_name}-e2e-s3-front-updated"
  event_pattern = jsonencode({
    "source" : ["aws.s3"],
    "detail-type" : ["Object Created"],
    "detail" : {
      "bucket" : { "name" : [var.s3_bucket_name] },
    }
  })
  tags = var.application_tag
}

resource "aws_cloudwatch_event_rule" "apprunner_deploy_succeeded" {
  count = var.enable_trigger_apprunner ? 1 : 0
  name  = "${var.project_name}-e2e-apprunner-deploy-succeeded"
  event_pattern = jsonencode({
    "source" : ["aws.apprunner"],
    "detail-type" : ["AppRunner Deployment State Change"],
    "detail" : {
      "ServiceArn" : [var.apprunner_service_arn],
      "Status" : ["SUCCEEDED"]
    }
  })
  tags = var.application_tag
}

resource "aws_cloudwatch_event_target" "s3_to_codebuild" {
  count    = var.enable_trigger_s3 ? 1 : 0
  rule     = aws_cloudwatch_event_rule.s3_front_updated[0].name
  arn      = aws_codebuild_project.this.arn
  role_arn = aws_iam_role.events_to_codebuild.arn
}

resource "aws_cloudwatch_event_target" "apprunner_to_codebuild" {
  count    = var.enable_trigger_apprunner ? 1 : 0
  rule     = aws_cloudwatch_event_rule.apprunner_deploy_succeeded[0].name
  arn      = aws_codebuild_project.this.arn
  role_arn = aws_iam_role.events_to_codebuild.arn
}
