# NFON OAuth 2.0 API Demo — C#

This demo shows how to authenticate with the NFON API using the **OAuth 2.0 Client Credentials Grant Flow** and make authenticated GET and PUT requests through the NFON API Gateway.

## What This Demo Does

1. **Obtains an access token** — sends a `POST` request to the OAuth2 token endpoint using your client credentials (HTTP Basic Auth with `grant_type=client_credentials`).
2. **Calls a GET endpoint** — sends an authenticated `GET` request to an NFON API endpoint and prints the response.
3. **Calls a PUT endpoint** — sends an authenticated `PUT` request with a JSON body to an NFON API endpoint and prints the response.

## Prerequisites

- **.NET 6.0 SDK** — [download](https://dotnet.microsoft.com/download/dotnet/6.0)

No third-party NuGet packages are required. The demo uses only built-in libraries (`System.Net.Http.HttpClient`, `System.Text.Json`).

## Configuration

Before running the demo, open `Program.cs` and replace the following placeholder values:

| Constant          | Description                                      | Default / Placeholder                                                             |
|-------------------|--------------------------------------------------|-----------------------------------------------------------------------------------|
| `AuthServerUrl`   | OAuth 2.0 token endpoint                         | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token`            |
| `ApiBaseUrl`      | NFON API Gateway base URL                        | `https://api.nfon.net/configuration/v1`                                           |
| `ApiGetEndpoint`  | GET endpoint URL — replace `{your-tenant-id}`    | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}`                  |
| `ApiPutEndpoint`  | PUT endpoint URL — replace `{your-tenant-id}`    | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}/basic-data`       |
| `ClientId`        | Your OAuth 2.0 client ID                         | `"your-client-id"`                                                                |
| `ClientSecret`    | Your OAuth 2.0 client secret                     | `"your-client-secret"`                                                            |

The demo validates the configuration at startup and exits with a clear error message if any placeholder values remain.

## Production URLs

| Purpose               | URL                                                                              |
|-----------------------|----------------------------------------------------------------------------------|
| API Gateway           | `https://api.nfon.net/`                                                          |
| OAuth 2.0 Token Endpoint | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token`        |

## Build and Run

```bash
# Navigate to the demo directory
cd csharp-demo

# Restore dependencies and build
dotnet build

# Run the demo (specify --get or --put)
dotnet run -- --get
```

### CLI Flags

You must specify which request to execute:

```bash
dotnet run -- --get          # run only the GET request
dotnet run -- --put          # run only the PUT request
dotnet run -- --get --put    # run both
```

