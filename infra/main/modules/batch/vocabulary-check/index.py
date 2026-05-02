import json
from typing import Any, Dict


def lambda_handler(event: Dict[str, Any], _: Any) -> Dict[str, Any]:
    print("Mock Vocabulary Check Lambda invoked.")
    print("Event:", json.dumps(event))
    return {
        "statusCode": 200,
        "body": json.dumps({"message": "Mock Vocabulary Check Lambda executed."}),
    }
