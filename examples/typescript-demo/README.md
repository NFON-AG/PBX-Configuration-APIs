# NFON OAuth 2.0 API Demo — TypeScript

This demo shows how to authenticate with the NFON API using the **OAuth 2.0 Client Credentials Grant Flow** and make authenticated GET and PUT requests through the NFON API Gateway.

## What This Demo Does

1. **Obtains an access token** — sends a `POST` request to the OAuth2 token endpoint using your client credentials (HTTP Basic Auth with `grant_type=client_credentials`).
2. **Calls a GET endpoint** — sends an authenticated `GET` request to an NFON API endpoint and prints the response.
3. **Calls a PUT endpoint** — sends an authenticated `PUT` request with a JSON body to an NFON API endpoint and prints the response.

## Prerequisites

- **Node.js 18** or later — [download](https://nodejs.org/)
- **npm** (included with Node.js)

No third-party runtime dependencies are required. The demo uses only Node.js built-in modules (`https`, `http`, `url`).

## Configuration

Before running the demo, open `src/OAuthClientCredentialsApiCaller.ts` and replace the following placeholder values:

| Constant        | Description                                      | Default / Placeholder                                                             |
|-----------------|--------------------------------------------------|-----------------------------------------------------------------------------------|
| `CLIENT_ID`     | Your OAuth 2.0 client ID                         | `"your-client-id"`                                                                |
| `CLIENT_SECRET` | Your OAuth 2.0 client secret                     | `"your-client-secret"`                                                            |
| `API_GET_ENDPOINT` | GET endpoint URL — replace `{your-tenant-id}` | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}`                  |
| `API_PUT_ENDPOINT` | PUT endpoint URL — replace `{your-tenant-id}` | `https://api.nfon.net/configuration/v1/tenants/{your-tenant-id}/basic-data`       |

The demo validates the configuration at startup and exits with a clear error message if any placeholder values remain.

## Production URLs

| Purpose               | URL                                                                              |
|-----------------------|----------------------------------------------------------------------------------|
| API Gateway           | `https://api.nfon.net/`                                                          |
| OAuth 2.0 Token Endpoint | `https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token`        |

## Build and Run

### Install dependencies

```bash
cd typescript-demo
npm install
```

### Run directly with ts-node

```bash
npm start -- --get
```

### Compile and run

```bash
npm run build
npm run run:compiled -- --get
```

### CLI Flags

You must specify which request to execute:

```bash
npm start -- --get          # run only the GET request
npm start -- --put          # run only the PUT request
npm start -- --get --put    # run both
```

## Project Structure

```
typescript-demo/
├── README.md
├── package.json
├── tsconfig.json
└── src/
    └── OAuthClientCredentialsApiCaller.ts   # Main demo script
```
