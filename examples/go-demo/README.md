# NFON OAuth 2.0 API Demo — Go

This demo shows how to authenticate with the NFON API using the **OAuth 2.0 Client Credentials Grant Flow** and make authenticated GET and PUT requests through the NFON API Gateway.

## What This Demo Does

1. **Obtains an access token** — sends a `POST` request to the OAuth2 token endpoint using your client credentials (HTTP Basic Auth with `grant_type=client_credentials`).
2. **Calls a GET endpoint** — sends an authenticated `GET` request to an NFON API endpoint and prints the response.
3. **Calls a PUT endpoint** — sends an authenticated `PUT` request with a JSON body to an NFON API endpoint and prints the response.

## Prerequisites

- **Go 1.21** or later — [download](https://go.dev/dl/)

No third-party dependencies are required. The demo uses only Go standard library packages (`net/http`, `encoding/json`).

## Configuration

Before running the demo, open `main.go` and replace the following placeholder values:

| Constant        | Description                                      | Default / Placeholder                                                             |
|-----------------|--------------------------------------------------|-----------------------------------------------------------------------------------|
| `clientID`      | Your OAuth 2.0 client ID                         | `"your-client-id"`                                                                |
| `clientSecret`  | Your OAuth 2.0 client secret                     | `"your-client-secret"`                                                            |
| `apiGetEndpoint`| GET endpoint URL — replace `{your-tenant-id}`    | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}`                  |
| `apiPutEndpoint`| PUT endpoint URL — replace `{your-tenant-id}`    | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}/basic-data`       |

The demo validates the configuration at startup and exits with a clear error message if any placeholder values remain.

## Production URLs

| Purpose               | URL                                                                              |
|-----------------------|----------------------------------------------------------------------------------|
| API Gateway           | `https://api.nfon.net/`                                                          |
| OAuth 2.0 Token Endpoint | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token`        |

## Build and Run

```bash
# Navigate to the demo directory
cd go-demo

# Build the binary
go build -o oauth-api-demo .

# Run the demo (specify -get or -put)
./oauth-api-demo -get
```

Or run directly without building:

```bash
cd go-demo
go run main.go -get
```

### CLI Flags

You must specify which request to execute:

```bash
go run main.go -get          # run only the GET request
go run main.go -put          # run only the PUT request
go run main.go -get -put     # run both
```

