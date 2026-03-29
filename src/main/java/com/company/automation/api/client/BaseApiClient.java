package com.company.automation.api.client;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Base REST API client using REST-assured.
 * Provides session management, auth headers, and common assertions.
 */
public abstract class BaseApiClient {

    private String baseUri;
    private String authToken;
    private Map<String, String> customHeaders = new HashMap<>();
    private Cookie sessionCookie;

    protected BaseApiClient() {
        this.baseUri = ConfigLoader.get(ConfigKeys.API_BASE_URL, "http://localhost:8080");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.config = RestAssuredConfig.newConfig()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", getTimeout())
                        .setParam("http.socket.timeout", getTimeout()));
    }

    // ── Base URL ─────────────────────────────────────────────────────────────

    protected void setBaseUri(String uri) {
        this.baseUri = uri;
    }

    protected String getBaseUri() {
        return baseUri;
    }

    private int getTimeout() {
        return ConfigLoader.getInt(ConfigKeys.API_TIMEOUT, 30_000);
    }

    // ── Authentication ───────────────────────────────────────────────────────

    /** Sets a Bearer token for all subsequent requests. */
    protected void setAuthToken(String token) {
        this.authToken = token;
    }

    /** Clears the stored auth token. */
    protected void clearAuthToken() {
        this.authToken = null;
    }

    /** Sets a session cookie for all subsequent requests. */
    protected void setSessionCookie(Cookie cookie) {
        this.sessionCookie = cookie;
    }

    /** Stores a named custom header (e.g. X-Request-ID). */
    protected void addHeader(String key, String value) {
        this.customHeaders.put(key, value);
    }

    /** Clears all custom headers. */
    protected void clearHeaders() {
        this.customHeaders.clear();
    }

    // ── Request builders ─────────────────────────────────────────────────────

    /**
     * Returns the base request specification with auth + custom headers applied.
     */
    protected RequestSpecification given() {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (authToken != null && !authToken.isBlank()) {
            spec.header("Authorization", "Bearer " + authToken);
        }

        customHeaders.forEach(spec::header);

        if (sessionCookie != null) {
            spec.cookie(sessionCookie);
        }

        return spec;
    }

    protected RequestSpecification givenNoAuth() {
        return RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .headers(customHeaders);
    }

    // ── HTTP verbs ───────────────────────────────────────────────────────────

    protected Response get(String path) {
        return logRequest("GET", path, () -> given().get(path));
    }

    protected Response post(String path, Object body) {
        return logRequest("POST", path, () -> given().body(body).post(path));
    }

    protected Response put(String path, Object body) {
        return logRequest("PUT", path, () -> given().body(body).put(path));
    }

    protected Response patch(String path, Object body) {
        return logRequest("PATCH", path, () -> given().body(body).patch(path));
    }

    protected Response delete(String path) {
        return logRequest("DELETE", path, () -> given().delete(path));
    }

    // ── File upload ─────────────────────────────────────────────────────────

    protected Response uploadFile(String path, File file, String formFieldName) {
        return logRequest("UPLOAD", path, () ->
                given().multiPart(formFieldName, file).post(path));
    }

    // ── Response helpers ─────────────────────────────────────────────────────

    protected int statusCode(Response response) {
        return response.statusCode();
    }

    protected String bodyAsString(Response response) {
        return response.asString();
    }

    protected ResponseBody body(Response response) {
        return response.body();
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    private Response logRequest(String method, String path, java.util.function.Supplier<Response> call) {
        AutomationLogger.debug("API {} {}{}", method, baseUri, path);
        Response response = call.get();
        AutomationLogger.debug("API response {} {}", response.statusCode(), path);
        return response;
    }
}
