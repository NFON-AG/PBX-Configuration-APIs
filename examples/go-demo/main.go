// NFON OAuth 2.0 Client Credentials API Demo (Go)
//
// This program demonstrates how to:
//   1. Obtain an OAuth 2.0 access token using the Client Credentials grant type
//   2. Call an NFON API GET endpoint with Bearer token authentication
//   3. Call an NFON API PUT endpoint with a JSON body and Bearer token authentication
//
// All API calls are routed through the NFON API Gateway (https://api.nfon.net/).
// Uses only Go standard library packages (net/http, encoding/json) — no third-party dependencies.
//
// Compatible with Go 1.21+
//
// Before running, replace clientID, clientSecret, and {your-tenant-id} with your actual values.

package main

import (
	"encoding/base64"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"strings"
)

// ---------------------------------------------------------------------------
// Configuration constants — replace these with your actual values
// ---------------------------------------------------------------------------

// OAuth 2.0 token endpoint
// TEST:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
// PROD:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
const authServerURL = "https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token"

// NFON API Gateway base URL — all API requests go through this gateway
const apiBaseURL = "https://api.nfon.net/configuration/v1/"

// GET endpoint
const apiGetEndpoint = apiBaseURL + "tenants/{your-tenant-id}"

// PUT endpoint
const apiPutEndpoint = apiBaseURL + "tenants/{your-tenant-id}/basic-data"

// OAuth 2.0 client credentials
const clientID = "your-client-id"
const clientSecret = "your-client-secret"

// ---------------------------------------------------------------------------
// Configuration validation
// ---------------------------------------------------------------------------

// validateConfiguration checks that all placeholder values have been replaced.
// Returns an error describing which values still need to be set.
func validateConfiguration() error {
	var errors []string

	if clientID == "your-client-id" || strings.TrimSpace(clientID) == "" {
		errors = append(errors, "clientID is not set")
	}
	if clientSecret == "your-client-secret" || strings.TrimSpace(clientSecret) == "" {
		errors = append(errors, "clientSecret is not set")
	}
	if strings.Contains(apiGetEndpoint, "{your-tenant-id}") || strings.TrimSpace(apiGetEndpoint) == "" {
		errors = append(errors, "apiGetEndpoint is not set or still contains {your-tenant-id}")
	}
	if strings.Contains(apiPutEndpoint, "{your-tenant-id}") || strings.TrimSpace(apiPutEndpoint) == "" {
		errors = append(errors, "apiPutEndpoint is not set or still contains {your-tenant-id}")
	}

	if len(errors) > 0 {
		return fmt.Errorf("demo configuration is incomplete: %s", strings.Join(errors, "; "))
	}
	return nil
}

// ---------------------------------------------------------------------------
// Token response model
// ---------------------------------------------------------------------------

// tokenResponse represents the JSON response from the OAuth2 token endpoint.
type tokenResponse struct {
	AccessToken string `json:"access_token"`
	TokenType   string `json:"token_type"`
	ExpiresIn   int    `json:"expires_in"`
	Scope       string `json:"scope"`
}

// ---------------------------------------------------------------------------
// Step 1: Obtain an OAuth 2.0 access token
// ---------------------------------------------------------------------------

// getAccessToken obtains an access token from the OAuth2 token endpoint
// using the OAuth 2.0 Client Credentials grant type.
//
// The client ID and secret are sent via HTTP Basic Authentication
// (Base64-encoded "client_id:client_secret").
func getAccessToken() (string, error) {
	tokenURL := authServerURL
	// Encode client credentials as Base64 for the Basic Authorization header
	credentials := clientID + ":" + clientSecret
	encodedCredentials := base64.StdEncoding.EncodeToString([]byte(credentials))

	// Request body specifies the Client Credentials grant type
	formData := url.Values{}
	formData.Set("grant_type", "client_credentials")

	req, err := http.NewRequest("POST", tokenURL, strings.NewReader(formData.Encode()))
	if err != nil {
		return "", fmt.Errorf("failed to create token request: %w", err)
	}

	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Authorization", "Basic "+encodedCredentials)
	req.Header.Set("User-Agent", "nfon-user-agent")

	fmt.Println("Requesting access token from:", tokenURL)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("token request failed: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read token response body: %w", err)
	}

	// Check for non-200 status code
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("failed to obtain access token. HTTP status: %d\nResponse: %s", resp.StatusCode, string(body))
	}

	// Parse the JSON response and extract the access token
	var tokenResp tokenResponse
	if err := json.Unmarshal(body, &tokenResp); err != nil {
		return "", fmt.Errorf("failed to parse token response JSON: %w", err)
	}

	if tokenResp.AccessToken == "" {
		return "", fmt.Errorf("access token not found in response: %s", string(body))
	}

	fmt.Println("Access token obtained successfully.")
	return tokenResp.AccessToken, nil
}

// ---------------------------------------------------------------------------
// Step 2: Call a GET endpoint
// ---------------------------------------------------------------------------

// callGetEndpoint sends an authenticated GET request to the configured API
// endpoint and prints the HTTP status code and response body.
func callGetEndpoint(accessToken string) error {
	endpoint := apiGetEndpoint
	req, err := http.NewRequest("GET", endpoint, nil)
	if err != nil {
		return fmt.Errorf("failed to create GET request: %w", err)
	}

	// Include the Bearer access token in the Authorization header
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("User-Agent", "nfon-user-agent")

	fmt.Printf("\nCalling GET %s\n", endpoint)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return fmt.Errorf("GET request failed: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read GET response body: %w", err)
	}

	fmt.Printf("GET Response Status: %d\n", resp.StatusCode)
	fmt.Printf("GET Response Body:\n%s\n", string(body))

	// Check for non-200 status code
	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("GET request failed. HTTP status: %d\nResponse: %s", resp.StatusCode, string(body))
	}

	return nil
}

// ---------------------------------------------------------------------------
// Step 3: Call a PUT endpoint
// ---------------------------------------------------------------------------

// callPutEndpoint sends an authenticated PUT request with a JSON body to
// the configured API endpoint and prints the HTTP status code and response body.
func callPutEndpoint(accessToken string) error {
	endpoint := apiPutEndpoint
	// JSON payload for updating basic-data
	putBody := `{"data": [{"name": "email", "value": "youremail@yourdomain.com"}]}`

	req, err := http.NewRequest("PUT", endpoint, strings.NewReader(putBody))
	if err != nil {
		return fmt.Errorf("failed to create PUT request: %w", err)
	}

	// Include the Bearer access token and JSON content type headers
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Accept", "*/*")
	req.Header.Set("User-Agent", "nfon-user-agent")

	fmt.Printf("\nCalling PUT %s\n", endpoint)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return fmt.Errorf("PUT request failed: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read PUT response body: %w", err)
	}

	fmt.Printf("PUT Response Status: %d\n", resp.StatusCode)
	fmt.Printf("PUT Response Body:\n%s\n", string(body))

	// Check for non-2xx status code
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("PUT request failed. HTTP status: %d\nResponse: %s", resp.StatusCode, string(body))
	}

	return nil
}

// ---------------------------------------------------------------------------
// Main entry point
// ---------------------------------------------------------------------------

// main orchestrates the full demo flow:
//  1. Obtain an OAuth 2.0 access token
//  2. Call the GET endpoint
//  3. Call the PUT endpoint
func main() {
	flagGet := flag.Bool("get", false, "Run only the GET request")
	flagPut := flag.Bool("put", false, "Run only the PUT request")
	flag.Parse()

	if !*flagGet && !*flagPut {
		fmt.Fprintln(os.Stderr, "Usage: oauth-api-demo -get | -put")
		fmt.Fprintln(os.Stderr, "  -get   Run only the GET request")
		fmt.Fprintln(os.Stderr, "  -put   Run only the PUT request")
		fmt.Fprintln(os.Stderr, "Specify -get or -put (or both) to choose which request to execute.")
		os.Exit(1)
	}

	// Validate configuration before proceeding
	if err := validateConfiguration(); err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}

	// Step 1: Obtain an OAuth2 access token
	accessToken, err := getAccessToken()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}

	// Step 2: Call the GET endpoint using the access token
	if *flagGet {
		if err := callGetEndpoint(accessToken); err != nil {
			fmt.Fprintf(os.Stderr, "Error: %v\n", err)
			os.Exit(1)
		}
	}

	// Step 3: Call the PUT endpoint using the access token
	if *flagPut {
		if err := callPutEndpoint(accessToken); err != nil {
			fmt.Fprintf(os.Stderr, "Error: %v\n", err)
			os.Exit(1)
		}
	}
}
