# NFON Service PBX-Configuration API Usage Manual

![Latest](https://img.shields.io/badge/docs-latest-blue)
- [Introduction](#introduction)
  - [Terms of Use](#terms-of-use)
  - [Important Notes](#important-notes)
  - [Support \& Feedback](#support--feedback)
- [API Endpoints](#api-endpoints)
- [API Credentials](#api-credentials)
- [Authentication](#authentication)
  - [Examples](#examples)
- [Migration from Service-Portal API](./api-gateway-migration-guide.md)

# Introduction

This is an early release which is a about to be made available soon which will very soon replace the known Service Portal API.

The NFON Service Portal API allows you to view and modify PBX-related

---

### Terms of Use
By accessing and using the NFON PBX-Configuration API, you agree to the [terms of use](https://www.nfon.com/en/legal/gtc-sla/).

---

### Important Notes

API endpoints may change: 
- Please subscribe to `API Breaking Changes` on the [NFON Status page](https://status.nfon.com) for updates.
- Please refer to the [latest API documentation](https://api.nfon.net/configuration/).

---

### Support & Feedback
NFON is committed to helping you integrate successfully with our APIs. Depending on the type of request or issue, please use the following channels:

#### Feature Requests
Got ideas to improve the API? Submit feature suggestions via Airfocus:
- [Submit in German language](https://nfon.airfocus.com/share/forms/a429ddf7abf54f6afed82d5a7327e030)
- [Submit in English language](https://nfon.airfocus.com/share/forms/648a1c17a224aa6e8937f8393f4bc21c)

#### Development Help
For best practices, implementation guidance, and community support:

- Join the [NFON Partner Portal](https://partners.nfon.com/)
- Contact your assigned sales representative  

#### Issues & Bugs
For technical issues or suspected bugs, please contact NFON Support directly.

## API Endpoints

Check the official [API Documentation](https://api.nfon.net/configuration/)  for latest endpoint references.

- **Base URL**: https://api.nfon.net/configuration/v1/
- **Architecture**: RESTful API
- **Data Format**: JSON

## API Credentials

To use the NFON PBX-Configuration API, you must first obtain **Client ID** and **Client Secret**.

Reach out to **integration@nfon.com** to obtain OAuth client credentials for the PBX Configuration API.

## Authentication

The new PBX Configuration API uses **OAuth 2.0** instead of API Key authentication.

You must obtain a Bearer token from the [NFON Identity Provider](https://sso.cloud-cfg.com/) and include it in the `Authorization` header:

```
Authorization: Bearer <your-access-token>
```

When using machine-to-machine authentication, you will need to implement the OAuth client credentials flow to programmatically retrieve access tokens.
When using the browser, you can authenticate via the [NFON Identity Provider](https://sso.cloud-cfg.com/) and use the obtained token for API calls ["Login with NFON"](https://www.nfon.com/en/integrations/login-with-nfon/).

---

### Examples

Below you’ll find working examples for API operations using various programming languages. These are designed to help you get started quickly and understand how to authenticate and interact with the PBX-Configuration API.

> 💡 **Cannot find your programming language of choice?** We recommend you to use an **AI assistant** to rewrite the examples to other programming languages. 
### Code examples:
- [with Go (Golang)](./examples/go-demo/)
- [with Java](./examples/java-demo/)
- [with Python](./examples/python-demo/)
- [with C# (.NET)](./examples/csharp-demo/)
- [with Node.js / TypeScript](./examples/typescript-demo/)

