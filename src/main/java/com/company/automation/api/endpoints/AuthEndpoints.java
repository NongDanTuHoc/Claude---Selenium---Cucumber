package com.company.automation.api.endpoints;

import com.company.automation.api.client.BaseApiClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Auth-related API endpoints — login, logout, token refresh.
 * Extend BaseApiClient to inherit given() and HTTP verb methods.
 */
public class AuthEndpoints extends BaseApiClient {

    public AuthEndpoints() {}

    // ── Endpoints ────────────────────────────────────────────────────────────

    /** POST /auth/login */
    public Response login(String username, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(username, password))
                .post("/auth/login");
    }

    /** POST /auth/logout */
    public Response logout() {
        return post("/auth/logout", null);
    }

    /** POST /auth/refresh — refreshes the stored Bearer token */
    public Response refreshToken(String refreshToken) {
        return givenNoAuth()
                .contentType(ContentType.JSON)
                .body(new RefreshRequest(refreshToken))
                .post("/auth/refresh");
    }

    /** GET /auth/me — returns current user profile */
    public Response getCurrentUser() {
        return get("/auth/me");
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RefreshRequest {
        public String refreshToken;

        public RefreshRequest() {}

        public RefreshRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
