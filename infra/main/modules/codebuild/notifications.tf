locals {
  enable_alert = var.alert_email != null && trimspace(var.alert_email) != ""
}

resource "aws_sns_topic" "codebuild_failed" {
  count = local.enable_alert ? 1 : 0
  name  = "${var.project_name}-codebuild-failed"
  tags  = var.application_tag
}

resource "aws_sns_topic_subscription" "email" {
  count     = local.enable_alert ? 1 : 0
  topic_arn = aws_sns_topic.codebuild_failed[0].arn
  protocol  = "email"
  endpoint  = var.alert_email
}

resource "aws_cloudwatch_event_rule" "codebuild_failed" {
  count = local.enable_alert ? 1 : 0
  name  = "${var.project_name}-codebuild-failed"

  event_pattern = jsonencode({
    "source" : ["aws.codebuild"],
    "detail-type" : ["CodeBuild Build State Change"],
    "detail" : {
      "build-status" : ["FAILED"],
      "project-name" : [aws_codebuild_project.this.name]
    }
  })

  tags = var.application_tag
}

resource "aws_cloudwatch_event_target" "to_sns" {
  count = local.enable_alert ? 1 : 0
  rule  = aws_cloudwatch_event_rule.codebuild_failed[0].name
  arn   = aws_sns_topic.codebuild_failed[0].arn

  input_transformer {
    input_paths = {
      project = "$.detail.project-name"
      status  = "$.detail.build-status"
      id      = "$.detail.build-id"
    }
    input_template = "\"[CodeBuild] <project> => <status>\\nBuildId: <id>\""
  }
}

data "aws_iam_policy_document" "sns_events_publish" {
  count = local.enable_alert ? 1 : 0
  statement {
    sid     = "AllowEventBridgeToPublish"
    effect  = "Allow"
    actions = ["SNS:Publish"]
    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }
    resources = [aws_sns_topic.codebuild_failed[0].arn]
    condition {
      test     = "ArnEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudwatch_event_rule.codebuild_failed[0].arn]
    }
  }
}

resource "aws_sns_topic_policy" "allow_events" {
  count  = local.enable_alert ? 1 : 0
  arn    = aws_sns_topic.codebuild_failed[0].arn
  policy = data.aws_iam_policy_document.sns_events_publish[0].json
}
