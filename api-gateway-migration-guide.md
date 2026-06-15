# PBX Configuration API Migration Guide

This guide helps you migrate existing integrations from the Service Portal API to the new NFON PBX Configuration API at `api.nfon.net`.

Note that the Service Portal API is about to be deprecated and will only be maintained for critical fixes.
All new development and integrations should use the new PBX Configuration API, which offers the same and an already broader functionality, improved performance, security, and a more consistent RESTful design.
It is the only API that will receive new features and updates going forward.

## Overview of Changes

| Aspect | Service Portal API                                                                                                          | PBX Configuration API                                                               |
|--------|-----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Base URL | `https://portal-api.nfon.net:8090/api/customers/{customerCode}/...`                                                         | `https://api.nfon.net/configuration/v1/tenants/{participantExternalIdentifier}/...` |
| Authentication | API Key (Basic Auth)                                                                                                        | OAuth 2.0 (Bearer Token)                                                            |
| OpenAPI Spec | [Service Portal API Specification](https://nfon-ag.github.io/Service-Portal-API-Specification/net/nfon/portal/api/Api.html) | [OpenAPI JSON](https://api.nfon.net/configuration/api/openapi.json)                 |
| Interactive Docs | —                                                                                                                           | [API Documentation](https://api.nfon.net/configuration/)                            |

## Authentication

The new PBX Configuration API uses **OAuth 2.0** instead of API Key authentication.

You must obtain a Bearer token from the [NFON Identity Provider](https://sso.cloud-cfg.com/) and include it in the `Authorization` header:

```
Authorization: Bearer <your-access-token>
```

When using machine-to-machine authentication, you will need to implement the OAuth client credentials flow to programmatically retrieve access tokens.
When using the browser, you can authenticate via the [NFON Identity Provider](https://sso.cloud-cfg.com/) and use the obtained token for API calls ["Login with NFON"](https://www.nfon.com/en/integrations/login-with-nfon/).

### OAuth Integration Demos

For working examples of the OAuth integration flow, see the [NFON Configuration API OAuth demo repository](README.md#examples):

- [C#](./examples/go-demo/csharp-demo)
- [Go](./examples/go-demo/go-demo)

- [Java](./examples/go-demo/java-demo)
- [Python](./examples/go-demo/python-demo)
- [TypeScript](./examples/go-demo/typescript-demo) 

> **Note:** Reach out to integration@nfon.com to obtain OAuth client credentials for the PBX Configuration API.

## Endpoint Migration

### Base Path Change

```
OLD: https://portal-api.nfon.net:8090/api/customers/{customerCode}
NEW: https://api.nfon.net/configuration/v1/tenants/{participantExternalIdentifier}
```

The `{customerCode}` (e.g. `K0611`) maps directly to `{participantExternalIdentifier}` — the value is the same.

### Path Segments Are Unchanged

All path segments after the base URL remain **identical** between the Service Portal API and the new PBX Configuration API. No renaming or restructuring is needed — simply replace the base URL prefix.

**Examples:**

| Service Portal URL                                                                                | PBX Configuration API URL |
|---------------------------------------------------------------------------------------------------|---------|
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/phone-extensions`                   | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/phone-extensions` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/phone-extensions/100/call-forwards` | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/phone-extensions/100/call-forwards` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/phone-extensions/100/function-keys` | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/phone-extensions/100/function-keys` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/phone-extensions/100/voice-mail`    | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/phone-extensions/100/voice-mail` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/queue-services`                     | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/queue-services` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/group-services`                     | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/group-services` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/targets/ivr-services`                       | `https://api.nfon.net/configuration/v1/tenants/K0611/targets/ivr-services` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/speed-dials`                                | `https://api.nfon.net/configuration/v1/tenants/K0611/speed-dials` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/phone-books`                                | `https://api.nfon.net/configuration/v1/tenants/K0611/phone-books` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/devices`                                    | `https://api.nfon.net/configuration/v1/tenants/K0611/devices` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/call-recording`                             | `https://api.nfon.net/configuration/v1/tenants/K0611/call-recording` |
| `https://portal-api.nfon.net:8090/api/customers/K0611/ip-whitelist`                               | `https://api.nfon.net/configuration/v1/tenants/K0611/ip-whitelist` |

### Top-Level Resources (no customer scope)

Top-level resources move from `/api/...` to the PBX Configuration API base without the `/api` prefix:

| Service Portal Path                | New Path |
|------------------------------------|----------|
| `/api/operators`                   | `/operators` |
| `/api/operators/{id}/softswitches` | `/operators/{id}/softswitches` |
| `/api/operators/{id}/tenants`      | `/operators/{id}/tenants` |
| `/api/device-types`                | `/device-types` |
| `/api/time-zones`                  | `/time-zones` |
| `/api/contract-types`              | `/contract-types` |
| `/api/pbx-groups`                  | `/pbx-groups` |
| `/api/directcall-numbers`          | `/directcall-numbers` |
| `/api/sip-servers`                 | `/sip-servers` |
| `/api/system-integrators/{id}`     | `/system-integrators/{id}` |

### New Endpoints (not available in Service Portal API)

The PBX Configuration API also exposes new functionality:

*Important*: Many of these endpoints require specific permissions that may not be granted by default or only to specific service providers upon request.
In case of questions about the permission required, consult the  [API Documentation](https://api.nfon.net/configuration/) which lists the required permission for every endpoint.
The standard permission is tenant.manager.

- `GET /tenants/{tenant}/licenses` — Retrieve licenses for a tenant
- `GET /tenants/{tenant}/licenses/usage` — License usage statistics
- `POST /tenants/{tenant}/licenses/assign` — Assign licenses to users/extensions
- `POST /tenants/{tenant}/licenses/unassign` — Unassign licenses
- `POST /tenants/{tenant}/devices/bulk` — Bulk device import
- `POST /tenants/{tenant}/devices/bulk/validate` — Validate devices before bulk import
- `POST /tenants/{tenant}/phone-extensions/bulk` — Bulk phone extension import/export
- `POST /tenants/{tenant}/phone-books/bulk` — Bulk phone book import/export
- `POST /tenants/{tenant}/speed-dials/bulk` — Bulk speed dial import/export
- `POST /tenants/{tenant}/virtual-fax-extensions/bulk` — Bulk virtual fax extension import/export
- `GET /tenants/{tenant}/references` — Find references to identifiers (extensions and service codes)
- `GET /tenants/{tenant}/queue-profiles` — Queue profile management
- `GET /tenants/{tenant}/jobs` — List async import/export jobs
- `GET /tenants/{tenant}/jobs/{eventId}` — Poll job status
- `GET /license-types` — List all available license types

## Quick Migration Checklist

1. **Obtain OAuth credentials** by reaching out to integration@nfon.com
2. **Replace authentication** — swap API Key / Basic Auth for OAuth Bearer token
3. **Update base URL** — `portal-api.nfon.net:8090/api/customers/{code}` → `api.nfon.net/configuration/v1/tenants/{code}`
4. **Test against the sandbox** — use the interactive docs to verify

## Migration Prompt for AI-Assisted Code Migration

Use the following prompt with an AI assistant (e.g. ChatGPT, Copilot, Claude) to automatically migrate your integration code:

> **Prompt:**
>
> Migrate my NFON Service Portal API integration to the new PBX Configuration API. Apply these changes:
>
> 1. Replace the base URL from `https://portal-api.nfon.net:8090/api/customers/{customerCode}` to `https://api.nfon.net/configuration/v1/tenants/{customerCode}`
> 2. Replace Basic Auth / API Key authentication with OAuth 2.0 Bearer token authentication
> 3. For top-level resources (operators, device-types, time-zones, etc.), replace `/api/` prefix with the PBX Configuration API base URL `https://api.nfon.net/configuration/v1/`
> 4. Keep the `{customerCode}` value the same — it maps to `{participantExternalIdentifier}`
> 5. All path segments after the base URL remain unchanged — do NOT rename any path segments
> 6. Add an OAuth token retrieval step before making API calls
>
> Working implementations can be found in the [examples](./examples/) folder.
>
> Here is my current code:
> ```
> <paste your code here>
> ```

## Support

- **API Documentation:** https://api.nfon.net/configuration/
- **OpenAPI Spec:** https://api.nfon.net/configuration/api/openapi.json
- **Service Portal API Specification:** https://nfon-ag.github.io/Service-Portal-API-Specification/net/nfon/portal/api/Api.html
- For questions about OAuth credentials or migration support, reach out to integration@nfon.com.

