/// <summary>
/// NFON OAuth 2.0 Client Credentials API Demo (C#)
///
/// This program demonstrates how to:
///   1. Obtain an OAuth 2.0 access token using the Client Credentials grant type
///   2. Call an NFON API GET endpoint with Bearer token authentication
///   3. Call an NFON API PUT endpoint with a JSON body and Bearer token authentication
///
/// All API calls are routed through the NFON API Gateway (https://api.nfon.net/).
/// Uses System.Net.Http.HttpClient and System.Text.Json — no third-party dependencies.
///
/// Compatible with .NET 6.0
///
/// Before running, replace ClientId, ClientSecret, and {your-tenant-id} with your actual values.
/// </summary>

using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;

// ---------------------------------------------------------------------------
// Configuration constants — replace these with your actual values
// ---------------------------------------------------------------------------

/// <summary>
/// OAuth 2.0 token endpoint
/// TEST:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
/// PROD:  https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token
/// </summary>
const string AuthServerUrl = "https://sso.cloud-cfg.com/realms/login/protocol/openid-connect/token";

/// <summary>NFON API Gateway base URL — all API requests go through this gateway</summary>
const string ApiBaseUrl = "https://api.nfon.net/configuration/v1";

/// <summary>GET endpoint</summary>
const string ApiGetEndpoint = $"{ApiBaseUrl}/tenants/{{your-tenant-id}}";

/// <summary>PUT endpoint</summary>
const string ApiPutEndpoint = $"{ApiBaseUrl}/tenants/{{your-tenant-id}}/basic-data";

/// <summary>OAuth 2.0 client credentials — replace with your own client ID and secret</summary>
const string ClientId = "your-client-id";
const string ClientSecret = "your-client-secret";

// ---------------------------------------------------------------------------
// Configuration validation
// ---------------------------------------------------------------------------

void ValidateConfiguration()
{
    var errors = new List<string>();

    if (ClientId == "your-client-id" || string.IsNullOrWhiteSpace(ClientId))
        errors.Add("ClientId is not set");
    if (ClientSecret == "your-client-secret" || string.IsNullOrWhiteSpace(ClientSecret))
        errors.Add("ClientSecret is not set");
    if (string.IsNullOrWhiteSpace(ApiGetEndpoint) || ApiGetEndpoint.Contains("{your-tenant-id}"))
        errors.Add("ApiGetEndpoint is not set or still contains {your-tenant-id}");
    if (string.IsNullOrWhiteSpace(ApiPutEndpoint) || ApiPutEndpoint.Contains("{your-tenant-id}"))
        errors.Add("ApiPutEndpoint is not set or still contains {your-tenant-id}");

    if (errors.Count > 0)
    {
        Console.Error.WriteLine($"Demo configuration is incomplete: {string.Join("; ", errors)}");
        Environment.Exit(1);
    }
}

// ---------------------------------------------------------------------------
// Create a shared HttpClient instance for all requests
// ---------------------------------------------------------------------------
using var httpClient = new HttpClient();

// Validate configuration before proceeding
ValidateConfiguration();

var flagGet = args.Contains("--get");
var flagPut = args.Contains("--put");

if (!flagGet && !flagPut)
{
    Console.Error.WriteLine("Usage: dotnet run -- --get | --put");
    Console.Error.WriteLine("  --get   Run only the GET request");
    Console.Error.WriteLine("  --put   Run only the PUT request");
    Console.Error.WriteLine("Specify --get or --put (or both) to choose which request to execute.");
    Environment.Exit(1);
}

try
{
    // Step 1: Obtain an OAuth2 access token
    var accessToken = await GetAccessTokenAsync();

    // Step 2: Call the GET endpoint using the access token
    if (flagGet)
    {
        await CallGetEndpointAsync(accessToken);
    }

    // Step 3: Call the PUT endpoint using the access token
    if (flagPut)
    {
        await CallPutEndpointAsync(accessToken);
    }
}
catch (HttpRequestException ex)
{
    Console.Error.WriteLine($"Error: {ex.Message}");
    Environment.Exit(1);
}

// ---------------------------------------------------------------------------
// Step 1: Obtain an OAuth 2.0 access token
// ---------------------------------------------------------------------------

/// <summary>
/// Obtains an access token from the OAuth2 token endpoint using the
/// OAuth 2.0 Client Credentials grant type.
///
/// The client ID and secret are sent via HTTP Basic Authentication
/// (Base64-encoded "client_id:client_secret").
/// </summary>
/// <returns>The access token string.</returns>
async Task<string> GetAccessTokenAsync()
{
    // Encode client credentials as Base64 for the Basic Authorization header
    var credentials = $"{ClientId}:{ClientSecret}";
    var encodedCredentials = Convert.ToBase64String(Encoding.UTF8.GetBytes(credentials));

    // Request body specifies the Client Credentials grant type
    var requestBody = new FormUrlEncodedContent(new[]
    {
        new KeyValuePair<string, string>("grant_type", "client_credentials")
    });

    using var request = new HttpRequestMessage(HttpMethod.Post, AuthServerUrl);
    request.Content = requestBody;
    request.Headers.Authorization = new AuthenticationHeaderValue("Basic", encodedCredentials);
    request.Headers.Add("User-Agent", "nfon-user-agent");

    Console.WriteLine($"Requesting access token from: {AuthServerUrl}");

    using var response = await httpClient.SendAsync(request);
    var body = await response.Content.ReadAsStringAsync();

    // Check for non-200 status code
    if (!response.IsSuccessStatusCode)
    {
        Console.Error.WriteLine($"Failed to obtain access token. HTTP status: {(int)response.StatusCode}");
        Console.Error.WriteLine($"Response: {body}");
        Environment.Exit(1);
    }

    // Parse the JSON response and extract the access token
    using var jsonDoc = JsonDocument.Parse(body);
    if (!jsonDoc.RootElement.TryGetProperty("access_token", out var tokenElement))
    {
        Console.Error.WriteLine($"Access token not found in response: {body}");
        Environment.Exit(1);
    }

    var accessToken = tokenElement.GetString();
    if (string.IsNullOrEmpty(accessToken))
    {
        Console.Error.WriteLine($"Access token is empty in response: {body}");
        Environment.Exit(1);
    }

    Console.WriteLine("Access token obtained successfully.");
    return accessToken;
}

// ---------------------------------------------------------------------------
// Step 2: Call a GET endpoint
// ---------------------------------------------------------------------------

/// <summary>
/// Sends an authenticated GET request to the configured API endpoint
/// and prints the HTTP status code and response body.
/// </summary>
/// <param name="accessToken">The Bearer token for authorization.</param>
async Task CallGetEndpointAsync(string accessToken)
{
    using var request = new HttpRequestMessage(HttpMethod.Get, ApiGetEndpoint);

    // Include the Bearer access token in the Authorization header
    request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);
    request.Headers.Add("User-Agent", "nfon-user-agent");

    Console.WriteLine($"\nCalling GET {ApiGetEndpoint}");

    using var response = await httpClient.SendAsync(request);
    var body = await response.Content.ReadAsStringAsync();

    Console.WriteLine($"GET Response Status: {(int)response.StatusCode}");
    Console.WriteLine($"GET Response Body:\n{body}");

    // Check for non-200 status code
    if (!response.IsSuccessStatusCode)
    {
        Console.Error.WriteLine($"GET request failed. HTTP status: {(int)response.StatusCode}");
        Console.Error.WriteLine($"Response: {body}");
        Environment.Exit(1);
    }
}

// ---------------------------------------------------------------------------
// Step 3: Call a PUT endpoint
// ---------------------------------------------------------------------------

/// <summary>
/// Sends an authenticated PUT request with a JSON body to the configured
/// API endpoint and prints the HTTP status code and response body.
/// </summary>
/// <param name="accessToken">The Bearer token for authorization.</param>
async Task CallPutEndpointAsync(string accessToken)
{
    // JSON payload for updating basic-data
    var putBody = new { data = new[] { new { name = "email", value = "youremail@yourdomain.com" } } };
    var jsonContent = new StringContent(
        JsonSerializer.Serialize(putBody),
        Encoding.UTF8,
        "application/json"
    );

    using var request = new HttpRequestMessage(HttpMethod.Put, ApiPutEndpoint);
    request.Content = jsonContent;

    // Include the Bearer access token in the Authorization header
    request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);
    request.Headers.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("*/*"));
    request.Headers.Add("User-Agent", "nfon-user-agent");

    Console.WriteLine($"\nCalling PUT {ApiPutEndpoint}");

    using var response = await httpClient.SendAsync(request);
    var body = await response.Content.ReadAsStringAsync();

    Console.WriteLine($"PUT Response Status: {(int)response.StatusCode}");
    Console.WriteLine($"PUT Response Body:\n{body}");

    // Check for non-2xx status code
    if ((int)response.StatusCode < 200 || (int)response.StatusCode >= 300)
    {
        Console.Error.WriteLine($"PUT request failed. HTTP status: {(int)response.StatusCode}");
        Console.Error.WriteLine($"Response: {body}");
        Environment.Exit(1);
    }
}
