package net.nfon.apidemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * NFON OAuth 2.0 Client Credentials API Demo (Java)
 *
 * Demonstrates how to:
 *   1. Obtain an OAuth 2.0 access token using the Client Credentials grant flow
 *   2. Call an NFON API GET endpoint with Bearer token authentication
 *   3. Call an NFON API PUT endpoint with a JSON body and Bearer token authentication
 *
 * All API calls are routed through the NFON API Gateway (https://api.nfon.net/).
 * Compatible with JDK 8+. Uses only standard library classes (no external dependencies).
 *
 * Before running, replace CLIENT_ID, CLIENT_SECRET, and {your-tenant-id} with your actual values.
 */
public class OAuthClientCredentialsApiCaller {

    // ======================== Configuration ========================
    // Replace these values with your actual NFON partner credentials.

    /**
     * OAuth 2.0 token endpoint
     */
    private static String AUTH_SERVER_URL = "https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token";

    /** NFON API Gateway base URL */
    private static String API_BASE_URL = "https://api.nfon.net/configuration/v1";

    /** GET endpoint URL — replace {your-tenant-id} with your tenant ID */
    private static String API_GET_ENDPOINT_URL = API_BASE_URL + "/tenants/{your-tenant-id}";

    /** PUT endpoint URL — replace {your-tenant-id} with your tenant ID */
    private static String API_PUT_ENDPOINT_URL = API_BASE_URL + "/tenants/{your-tenant-id}/basic-data";

    /** OAuth2 client ID — replace with your client ID */
    private static String CLIENT_ID = "your-client-id";

    /** OAuth2 client secret — replace with your client secret */
    private static String CLIENT_SECRET = "your-client-secret";

    // ======================== Configuration Validation ========================

    /**
     * Validates that all required configuration values have been replaced
     * from their placeholder defaults. Exits with a clear error message
     * if any placeholders remain.
     */
    private static void validateConfiguration() {
        List<String> errors = new ArrayList<>();

        if ("your-client-id".equals(CLIENT_ID) || CLIENT_ID == null || CLIENT_ID.trim().isEmpty()) {
            errors.add("CLIENT_ID is not set");
        }
        if ("your-client-secret".equals(CLIENT_SECRET) || CLIENT_SECRET == null || CLIENT_SECRET.trim().isEmpty()) {
            errors.add("CLIENT_SECRET is not set");
        }
        if (API_GET_ENDPOINT_URL == null || API_GET_ENDPOINT_URL.trim().isEmpty() || API_GET_ENDPOINT_URL.contains("{your-tenant-id}")) {
            errors.add("API_GET_ENDPOINT_URL is not set or still contains {your-tenant-id}");
        }
        if (API_PUT_ENDPOINT_URL == null || API_PUT_ENDPOINT_URL.trim().isEmpty() || API_PUT_ENDPOINT_URL.contains("{your-tenant-id}")) {
            errors.add("API_PUT_ENDPOINT_URL is not set or still contains {your-tenant-id}");
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Demo configuration is incomplete: ");
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) sb.append("; ");
                sb.append(errors.get(i));
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    // ======================== Main ========================

    public static void main(String[] args) {
        // Validate configuration before proceeding
        validateConfiguration();

        boolean flagGet = false;
        boolean flagPut = false;

        for (String arg : args) {
            if ("--get".equals(arg)) flagGet = true;
            if ("--put".equals(arg)) flagPut = true;
        }

        if (!flagGet && !flagPut) {
            System.err.println("Usage: OAuthClientCredentialsApiCaller --get | --put");
            System.err.println("  --get   Run only the GET request");
            System.err.println("  --put   Run only the PUT request");
            System.err.println("Specify --get or --put (or both) to choose which request to execute.");
            System.exit(1);
        }

        try {
            // Step 1: Obtain an OAuth 2.0 access token using Client Credentials flow
            System.out.println("Step 1: Obtaining access token...");
            String accessToken = getAccessToken();
            System.out.println("Access token obtained successfully.\n");

            // Step 2: Call the GET endpoint with Bearer token authentication
            if (flagGet) {
                System.out.println("Step 2: Calling GET endpoint...");
                callGetEndpoint(accessToken);
            }

            // Step 3: Call the PUT endpoint with a JSON body and Bearer token authentication
            if (flagPut) {
                System.out.println("\nStep 3: Calling PUT endpoint...");
                callPutEndpoint(accessToken);
            }

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== Token Acquisition ========================

    /**
     * Obtains an OAuth 2.0 access token using the Client Credentials grant flow.
     *
     * Sends a POST request to the OAuth2 token endpoint with:
     *   - HTTP Basic Authentication (Base64-encoded client_id:client_secret)
     *   - grant_type=client_credentials in the request body
     *
     * @return the access token string
     * @throws Exception if the token request fails or the response cannot be parsed
     */
    private static String getAccessToken() throws Exception {
        URL url = new URL(AUTH_SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        connection.setRequestProperty("User-Agent", "nfon-user-agent");

        connection.setDoOutput(true);

        String requestBody = "grant_type=client_credentials";
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to obtain access token. HTTP status code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String jsonResponse = response.toString();
        String key = "\"access_token\"";
        int keyIndex = jsonResponse.indexOf(key);
        if (keyIndex != -1) {
            int colonIndex = jsonResponse.indexOf(":", keyIndex + key.length());
            int valueStart = jsonResponse.indexOf("\"", colonIndex + 1);
            int valueEnd = jsonResponse.indexOf("\"", valueStart + 1);
            return jsonResponse.substring(valueStart + 1, valueEnd);
        } else {
            throw new RuntimeException("access_token not found in response: " + jsonResponse);
        }
    }

    // ======================== GET Request ========================

    /**
     * Calls the API GET endpoint using Bearer token authentication.
     * Prints the HTTP status code and response body.
     *
     * @param accessToken the OAuth2 access token
     * @throws Exception if there is an error calling the API
     */
    private static void callGetEndpoint(String accessToken) throws Exception {
        URL url = new URL(API_GET_ENDPOINT_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("User-Agent", "nfon-user-agent");

        int responseCode = connection.getResponseCode();

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        System.out.println("GET Response Status Code: " + responseCode);
        System.out.println("GET Response Body:");
        System.out.println(response.toString());

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("GET request failed. HTTP status code: " + responseCode
                    + ", Body: " + response.toString());
        }
    }

    // ======================== PUT Request ========================

    /**
     * Calls the API PUT endpoint with a JSON body and Bearer token authentication.
     * Prints the HTTP status code and response body.
     *
     * @param accessToken the OAuth2 access token
     * @throws Exception if there is an error calling the API
     */
    private static void callPutEndpoint(String accessToken) throws Exception {
        URL url = new URL(API_PUT_ENDPOINT_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");

        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", "nfon-user-agent");

        connection.setDoOutput(true);

        String jsonBody = "{\"data\": [{\"name\": \"email\", \"value\": \"youremail@yourdomain.com\"}]}";
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        System.out.println("PUT Response Status Code: " + responseCode);
        System.out.println("PUT Response Body:");
        System.out.println(response.toString());

        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("PUT request failed. HTTP status code: " + responseCode
                    + ", Body: " + response.toString());
        }
    }
}
