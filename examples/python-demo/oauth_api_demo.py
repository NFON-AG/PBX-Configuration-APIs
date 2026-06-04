"""
NFON OAuth 2.0 Client Credentials API Demo (Python)

This script demonstrates how to:
  1. Obtain an OAuth 2.0 access token using the Client Credentials grant type
  2. Call an NFON API GET endpoint with Bearer token authentication
  3. Call an NFON API PUT endpoint with a JSON body and Bearer token authentication

All API calls are routed through the NFON API Gateway (https://api.nfon.net/).
Uses the 'requests' library for HTTP calls — install via: pip install -r requirements.txt

Compatible with Python 3.8+

Before running, replace CLIENT_ID, CLIENT_SECRET, and {your-tenant-id} with your actual values.
"""

import argparse
import base64
import sys

import requests

# ---------------------------------------------------------------------------
# Configuration constants — replace these with your actual values
# ---------------------------------------------------------------------------

# OAuth 2.0 token endpoint
# TEST:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
# PROD:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
AUTH_SERVER_URL = "https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token"

# NFON API Gateway base URL — all API requests go through this gateway
API_BASE_URL = "https://api.nfon.net/configuration/v1"

# GET endpoint
API_GET_ENDPOINT = f"{API_BASE_URL}/tenants/{{your-tenant-id}}"

# PUT endpoint
API_PUT_ENDPOINT = f"{API_BASE_URL}/tenants/{{your-tenant-id}}/basic-data"

# OAuth 2.0 client credentials — replace with your own client ID and secret
CLIENT_ID = "your-client-id"
CLIENT_SECRET = "your-client-secret"


# ---------------------------------------------------------------------------
# Configuration validation
# ---------------------------------------------------------------------------


def validate_configuration() -> None:
    """Validate that all placeholder values have been replaced.

    Raises:
        SystemExit: If any configuration values are still placeholders.
    """
    errors = []

    if CLIENT_ID == "your-client-id" or not CLIENT_ID.strip():
        errors.append("CLIENT_ID is not set")
    if CLIENT_SECRET == "your-client-secret" or not CLIENT_SECRET.strip():
        errors.append("CLIENT_SECRET is not set")
    if "{your-tenant-id}" in API_GET_ENDPOINT or not API_GET_ENDPOINT.strip():
        errors.append("API_GET_ENDPOINT is not set or still contains {your-tenant-id}")
    if "{your-tenant-id}" in API_PUT_ENDPOINT or not API_PUT_ENDPOINT.strip():
        errors.append("API_PUT_ENDPOINT is not set or still contains {your-tenant-id}")

    if errors:
        print(f"Demo configuration is incomplete: {'; '.join(errors)}", file=sys.stderr)
        sys.exit(1)


# ---------------------------------------------------------------------------
# Step 1: Obtain an OAuth 2.0 access token
# ---------------------------------------------------------------------------


def get_access_token() -> str:
    """Obtain an access token from the OAuth2 token endpoint.

    Uses the OAuth 2.0 Client Credentials grant type with HTTP Basic
    Authentication (Base64-encoded "client_id:client_secret").

    Returns:
        The access token string.

    Raises:
        SystemExit: If the token request fails or the response is invalid.
    """
    # Encode client credentials as Base64 for the Basic Authorization header
    credentials = f"{CLIENT_ID}:{CLIENT_SECRET}"
    encoded_credentials = base64.b64encode(credentials.encode()).decode()

    # Request body specifies the Client Credentials grant type
    data = {"grant_type": "client_credentials"}
    headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": f"Basic {encoded_credentials}",
        "User-Agent": "nfon-user-agent",
    }

    print(f"Requesting access token from: {AUTH_SERVER_URL}")

    response = requests.post(AUTH_SERVER_URL, data=data, headers=headers)

    # Check for non-200 status code
    if response.status_code != 200:
        print(
            f"Failed to obtain access token. HTTP status: {response.status_code}",
            file=sys.stderr,
        )
        print(f"Response: {response.text}", file=sys.stderr)
        sys.exit(1)

    # Parse the JSON response and extract the access token
    token_data = response.json()
    access_token = token_data.get("access_token")

    if not access_token:
        print(f"Access token not found in response: {response.text}", file=sys.stderr)
        sys.exit(1)

    print("Access token obtained successfully.")
    return access_token


# ---------------------------------------------------------------------------
# Step 2: Call a GET endpoint
# ---------------------------------------------------------------------------


def call_get_endpoint(access_token: str) -> None:
    """Send an authenticated GET request to the configured API endpoint.

    Prints the HTTP status code and response body.

    Args:
        access_token: The Bearer token for authorization.
    """
    # Include the Bearer access token in the Authorization header
    headers = {"Authorization": f"Bearer {access_token}", "User-Agent": "nfon-user-agent"}

    print(f"\nCalling GET {API_GET_ENDPOINT}")

    response = requests.get(API_GET_ENDPOINT, headers=headers)

    print(f"GET Response Status: {response.status_code}")
    print(f"GET Response Body:\n{response.text}")

    # Check for non-200 status code
    if response.status_code != 200:
        print(
            f"GET request failed. HTTP status: {response.status_code}",
            file=sys.stderr,
        )
        print(f"Response: {response.text}", file=sys.stderr)
        sys.exit(1)


# ---------------------------------------------------------------------------
# Step 3: Call a PUT endpoint
# ---------------------------------------------------------------------------


def call_put_endpoint(access_token: str) -> None:
    """Send an authenticated PUT request with a JSON body to the configured API endpoint.

    Prints the HTTP status code and response body.

    Args:
        access_token: The Bearer token for authorization.
    """
    # JSON payload for updating basic-data
    put_body = {"data": [{"name": "email", "value": "youremail@yourdomain.com"}]}

    # Include the Bearer access token and JSON content type headers
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
        "Accept": "*/*",
        "User-Agent": "nfon-user-agent",
    }

    print(f"\nCalling PUT {API_PUT_ENDPOINT}")

    response = requests.put(API_PUT_ENDPOINT, json=put_body, headers=headers)

    print(f"PUT Response Status: {response.status_code}")
    print(f"PUT Response Body:\n{response.text}")

    # Check for non-2xx status code
    if response.status_code < 200 or response.status_code >= 300:
        print(
            f"PUT request failed. HTTP status: {response.status_code}",
            file=sys.stderr,
        )
        print(f"Response: {response.text}", file=sys.stderr)
        sys.exit(1)


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------


def main() -> None:
    """Orchestrate the full demo flow.

    Usage:
        python oauth_api_demo.py --get     # runs only GET
        python oauth_api_demo.py --put     # runs only PUT
        python oauth_api_demo.py --get --put  # runs both
    """
    parser = argparse.ArgumentParser(description="NFON OAuth 2.0 API Demo")
    parser.add_argument("--get", action="store_true", help="Run only the GET request")
    parser.add_argument("--put", action="store_true", help="Run only the PUT request")
    args = parser.parse_args()

    if not args.get and not args.put:
        parser.error("Specify --get or --put (or both) to choose which request to execute.")

    # Validate configuration before proceeding
    validate_configuration()

    try:
        # Step 1: Obtain an OAuth2 access token
        access_token = get_access_token()

        # Step 2: Call the GET endpoint using the access token
        if args.get:
            call_get_endpoint(access_token)

        # Step 3: Call the PUT endpoint using the access token
        if args.put:
            call_put_endpoint(access_token)

    except requests.exceptions.RequestException as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
