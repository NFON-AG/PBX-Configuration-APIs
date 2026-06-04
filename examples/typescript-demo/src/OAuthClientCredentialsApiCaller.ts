/**
 * NFON OAuth 2.0 Client Credentials API Demo (TypeScript / Node.js)
 *
 * This script demonstrates how to:
 *   1. Obtain an OAuth 2.0 access token using the Client Credentials grant type
 *   2. Call an NFON API GET endpoint with Bearer token authentication
 *   3. Call an NFON API PUT endpoint with a JSON body and Bearer token authentication
 *
 * All API calls are routed through the NFON API Gateway (https://api.nfon.net/).
 * Uses only Node.js built-in modules (https, http, url) — no third-party dependencies at runtime.
 *
 * Compatible with Node.js 18+
 *
 * Before running, replace CLIENT_ID, CLIENT_SECRET, and {your-tenant-id} with your actual values.
 */

import https from 'https';
import http from 'http';
import { URL } from 'url';

// ---------------------------------------------------------------------------
// Configuration constants — replace these with your actual values
// ---------------------------------------------------------------------------

// OAuth 2.0 token endpoint
// TEST:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
// PROD:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
const AUTH_SERVER_URL = "https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token";

// NFON API Gateway base URL — all API requests go through this gateway
const API_BASE_URL = "https://api.nfon.net/configuration/v1";

// GET endpoint
const API_GET_ENDPOINT = `${API_BASE_URL}/tenants/{your-tenant-id}`;

// PUT endpoint
const API_PUT_ENDPOINT = `${API_BASE_URL}/tenants/{your-tenant-id}/basic-data`;

// OAuth 2.0 client credentials — replace with your own client ID and secret
const CLIENT_ID = "your-client-id";
const CLIENT_SECRET = "your-client-secret";

// ---------------------------------------------------------------------------
// Configuration validation
// ---------------------------------------------------------------------------

/**
 * Validates that all placeholder values have been replaced.
 * Throws an error if any configuration is still using defaults.
 */
function validateConfiguration(): void {
    const errors: string[] = [];

    if (CLIENT_ID === "your-client-id" || CLIENT_ID.trim() === "") {
        errors.push("CLIENT_ID is not set");
    }
    if (CLIENT_SECRET === "your-client-secret" || CLIENT_SECRET.trim() === "") {
        errors.push("CLIENT_SECRET is not set");
    }
    if (API_GET_ENDPOINT.includes("{your-tenant-id}") || API_GET_ENDPOINT.trim() === "") {
        errors.push("API_GET_ENDPOINT is not set or still contains {your-tenant-id}");
    }
    if (API_PUT_ENDPOINT.includes("{your-tenant-id}") || API_PUT_ENDPOINT.trim() === "") {
        errors.push("API_PUT_ENDPOINT is not set or still contains {your-tenant-id}");
    }

    if (errors.length > 0) {
        console.error(`Demo configuration is incomplete: ${errors.join("; ")}`);
        process.exit(1);
    }
}

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface TokenResponse {
    access_token: string;
    token_type: string;
    expires_in: number;
    scope?: string;
}

// ---------------------------------------------------------------------------
// HTTP helper
// ---------------------------------------------------------------------------

/**
 * Makes an HTTP/HTTPS request and returns the status code and response body.
 */
function makeRequest(
    urlString: string,
    method: string,
    headers: Record<string, string>,
    body?: string
): Promise<{ statusCode: number; body: string }> {
    return new Promise((resolve, reject) => {
        const url = new URL(urlString);
        const isHttps = url.protocol === 'https:';
        const httpModule = isHttps ? https : http;

        const options: https.RequestOptions = {
            hostname: url.hostname,
            port: url.port || (isHttps ? 443 : 80),
            path: url.pathname + url.search,
            method: method,
            headers: headers
        };

        const req = httpModule.request(options, (res) => {
            let data = '';
            res.setEncoding('utf8');

            res.on('data', (chunk) => {
                data += chunk;
            });

            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode || 0,
                    body: data
                });
            });
        });

        req.on('error', (error) => {
            reject(error);
        });

        if (body) {
            req.write(body);
        }

        req.end();
    });
}

// ---------------------------------------------------------------------------
// Step 1: Obtain an OAuth 2.0 access token
// ---------------------------------------------------------------------------

/**
 * Obtains an access token from the OAuth2 token endpoint using the
 * OAuth 2.0 Client Credentials grant type.
 *
 * The client ID and secret are sent via HTTP Basic Authentication
 * (Base64-encoded "client_id:client_secret").
 *
 * @returns the access token string
 */
async function getAccessToken(): Promise<string> {
    // Encode client credentials as Base64 for the Basic Authorization header
    const credentials = `${CLIENT_ID}:${CLIENT_SECRET}`;
    const encodedCredentials = Buffer.from(credentials, 'utf-8').toString('base64');

    const headers: Record<string, string> = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Basic ${encodedCredentials}`,
        'User-Agent': 'nfon-user-agent'
    };

    // Request body specifies the Client Credentials grant type
    const requestBody = 'grant_type=client_credentials';

    console.log("Requesting access token from:", AUTH_SERVER_URL);
    const response = await makeRequest(AUTH_SERVER_URL, 'POST', headers, requestBody);

    if (response.statusCode !== 200) {
        throw new Error(
            `Failed to obtain access token. HTTP status: ${response.statusCode}\nResponse: ${response.body}`
        );
    }

    // Parse the JSON response and extract the access token
    const tokenResponse: TokenResponse = JSON.parse(response.body);

    if (!tokenResponse.access_token) {
        throw new Error(`Access token not found in response: ${response.body}`);
    }

    console.log("Access token obtained successfully.");
    return tokenResponse.access_token;
}

// ---------------------------------------------------------------------------
// Step 2: Call a GET endpoint
// ---------------------------------------------------------------------------

/**
 * Sends an authenticated GET request to the configured API endpoint and
 * prints the HTTP status code and response body.
 *
 * @param accessToken the Bearer token obtained from getAccessToken()
 */
async function callGetEndpoint(accessToken: string): Promise<void> {
    const headers: Record<string, string> = {
        'Authorization': `Bearer ${accessToken}`,
        'User-Agent': 'nfon-user-agent'
    };

    console.log(`\nCalling GET ${API_GET_ENDPOINT}`);
    const response = await makeRequest(API_GET_ENDPOINT, 'GET', headers);

    console.log(`GET Response Status: ${response.statusCode}`);
    console.log(`GET Response Body:\n${response.body}`);

    if (response.statusCode !== 200) {
        throw new Error(
            `GET request failed. HTTP status: ${response.statusCode}\nResponse: ${response.body}`
        );
    }
}

// ---------------------------------------------------------------------------
// Step 3: Call a PUT endpoint
// ---------------------------------------------------------------------------

/**
 * Sends an authenticated PUT request with a JSON body to the configured
 * API endpoint and prints the HTTP status code and response body.
 *
 * @param accessToken the Bearer token obtained from getAccessToken()
 */
async function callPutEndpoint(accessToken: string): Promise<void> {
    // JSON payload for updating basic-data
    const putBody = JSON.stringify({
        data: [{ name: "email", value: "youremail@yourdomain.com" }]
    });

    const headers: Record<string, string> = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        'Accept': '*/*',
        'User-Agent': 'nfon-user-agent'
    };

    console.log(`\nCalling PUT ${API_PUT_ENDPOINT}`);
    const response = await makeRequest(API_PUT_ENDPOINT, 'PUT', headers, putBody);

    console.log(`PUT Response Status: ${response.statusCode}`);
    console.log(`PUT Response Body:\n${response.body}`);

    if (response.statusCode < 200 || response.statusCode >= 300) {
        throw new Error(
            `PUT request failed. HTTP status: ${response.statusCode}\nResponse: ${response.body}`
        );
    }
}

// ---------------------------------------------------------------------------
// Main entry point
// ---------------------------------------------------------------------------

/**
 * Orchestrates the full demo flow:
 *   1. Obtain an OAuth 2.0 access token
 *   2. Call the GET endpoint (unless --put flag is used)
 *   3. Call the PUT endpoint (unless --get flag is used)
 *
 * Usage:
 *   npm start -- --get       # runs only GET
 *   npm start -- --put       # runs only PUT
 *   npm start -- --get --put # runs both
 */
async function main(): Promise<void> {
    const args = process.argv.slice(2);
    const flagGet = args.includes('--get');
    const flagPut = args.includes('--put');

    if (!flagGet && !flagPut) {
        console.error("Usage: npm start -- --get | --put");
        console.error("  --get   Run only the GET request");
        console.error("  --put   Run only the PUT request");
        console.error("Specify --get or --put (or both) to choose which request to execute.");
        process.exit(1);
    }

    // Validate configuration before proceeding
    validateConfiguration();

    try {
        // Step 1: Obtain an OAuth2 access token
        const accessToken = await getAccessToken();

        // Step 2: Call the GET endpoint using the access token
        if (flagGet) {
            await callGetEndpoint(accessToken);
        }

        // Step 3: Call the PUT endpoint using the access token
        if (flagPut) {
            await callPutEndpoint(accessToken);
        }

    } catch (error) {
        if (error instanceof Error) {
            console.error(`Error occurred: ${error.message}`);
            console.error(error.stack);
        } else {
            console.error('An unknown error occurred:', error);
        }
        process.exit(1);
    }
}

// Run the main function
main();
