# NFON OAuth 2.0 API Demo — Java

This demo shows how to authenticate with the NFON API using the **OAuth 2.0 Client Credentials Grant Flow** and make authenticated GET and PUT requests through the NFON API Gateway.

## What This Demo Does

1. **Obtains an access token** — sends a `POST` request to the OAuth2 token endpoint using your client credentials (HTTP Basic Auth with `grant_type=client_credentials`).
2. **Calls a GET endpoint** — sends an authenticated `GET` request to an NFON API endpoint and prints the response.
3. **Calls a PUT endpoint** — sends an authenticated `PUT` request with a JSON body to an NFON API endpoint and prints the response.

## Prerequisites

- **Java 8 or higher (JDK 8+)** — [download](https://adoptium.net/temurin/releases/)
- **Apache Maven 3.x** — [download](https://maven.apache.org/download.cgi)

No third-party runtime dependencies are required. The demo uses only standard JDK classes (`java.net.HttpURLConnection`).

## Configuration

Before running the demo, open the source file and replace the placeholder values:

**File:** `src/main/java/net/nfon/apidemo/OAuthClientCredentialsApiCaller.java`

| Constant              | Description                                      | Default / Placeholder                |
|-----------------------|--------------------------------------------------|--------------------------------------|
| `AUTH_SERVER_URL`     | OAuth 2.0 token endpoint                         | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token` |
| `API_BASE_URL`        | NFON API Gateway base URL                        | `https://api.nfon.net/configuration/v1` |
| `API_GET_ENDPOINT_URL`| GET endpoint — replace `{your-tenant-id}`        | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}` |
| `API_PUT_ENDPOINT_URL`| PUT endpoint — replace `{your-tenant-id}`        | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}/basic-data` |
| `CLIENT_ID`           | Your OAuth 2.0 client ID                         | `"your-client-id"`                   |
| `CLIENT_SECRET`       | Your OAuth 2.0 client secret                     | `"your-client-secret"`               |

The demo validates the configuration at startup and exits with a clear error message if any placeholder values remain.

## Production URLs

| Purpose               | URL                                                                              |
|-----------------------|----------------------------------------------------------------------------------|
| API Gateway           | `https://api.nfon.net/`                                                          |
| OAuth 2.0 Token Endpoint | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token`        |

## Build and Run

```bash
# Navigate to the demo directory
cd java-demo

# Build the project
mvn clean install

# Run the demo (specify --get or --put)
mvn exec:java -Dexec.mainClass="net.nfon.apidemo.OAuthClientCredentialsApiCaller" -Dexec.args="--get"
```

Or compile and run manually:

```bash
mvn clean install
java -cp target/classes net.nfon.apidemo.OAuthClientCredentialsApiCaller --get
```

### CLI Flags

You must specify which request to execute:

```bash
mvn exec:java -Dexec.mainClass="net.nfon.apidemo.OAuthClientCredentialsApiCaller" -Dexec.args="--get"        # run only GET
mvn exec:java -Dexec.mainClass="net.nfon.apidemo.OAuthClientCredentialsApiCaller" -Dexec.args="--put"        # run only PUT
mvn exec:java -Dexec.mainClass="net.nfon.apidemo.OAuthClientCredentialsApiCaller" -Dexec.args="--get --put"  # run both
```

