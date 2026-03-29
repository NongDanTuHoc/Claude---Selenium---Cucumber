package com.company.automation.testdata.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a user record loaded from testdata/login/valid_users.json
 * and similar files.
 *
 * Example JSON:
 * <pre>
 * {
 *   "username": "admin",
 *   "password": "secret123",
 *   "email": "admin@example.com",
 *   "role": "admin",
 *   "enabled": true
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTestData {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role;

    @JsonProperty("enabled")
    private boolean enabled;

    public UserTestData() {}

    public UserTestData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "UserTestData{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
