import os
import boto3


def handler(event, _):
    client = boto3.client("apprunner")
    service_arn = os.environ["SERVICE_ARN"]
    action = event.get("action") or os.environ.get("ACTION")
    if action == "pause":
        client.pause_service(ServiceArn=service_arn)
        return {"status": "paused"}
    elif action == "resume":
        client.resume_service(ServiceArn=service_arn)
        return {"status": "resumed"}
    else:
        return {"status": "noop", "reason": "unknown action"}
