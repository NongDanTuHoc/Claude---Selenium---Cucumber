package com.company.automation.stepdefinitions.login;

import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.pages.login.LoginPage;
import com.company.automation.stepdefinitions.BaseStepDefs;
import com.company.automation.testdata.TestDataLoader;
import com.company.automation.testdata.models.UserTestData;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Login feature step definitions.
 * Delegates all UI interactions to LoginPage — stays thin.
 */
public class LoginStepDefs extends BaseStepDefs {

    private LoginPage loginPage;

    // ── Shared test state ─────────────────────────────────────────────────────

    private String lastErrorMessage;
    private UserTestData lastUser;

    // ── Background / Setup ───────────────────────────────────────────────────

    @Given("the user is on the login page")
    public void user_on_login_page() {
        String baseUrl = ConfigLoader.get("app.baseurl", "https://example.com");
        String loginPath = ConfigLoader.get("app.login.path", "/login");
        openUrl(baseUrl + loginPath);
        loginPage = new LoginPage(driver());
        assertTrue(loginPage.isLoaded(), "Login page did not load correctly");
        AutomationLogger.info("Login page opened");
    }

    // ── Successful login ─────────────────────────────────────────────────────

    @When("the user logs in with valid credentials from test data {string}")
    public void user_logs_in_with_valid_credentials(String dataFile) {
        List<UserTestData> users = TestDataLoader.loadArrayAs("login/" + dataFile, UserTestData.class);
        assertFalse(users.isEmpty(), "No users found in: " + dataFile);
        lastUser = users.get(0);
        loginPage.login(lastUser.getUsername(), lastUser.getPassword());
        AutomationLogger.info("Logged in as: {}", lastUser.getUsername());
    }

    @When("the user logs in with username {string} and password {string}")
    public void user_logs_in_with_username_password(String username, String password) {
        loginPage.login(username, password);
        AutomationLogger.info("Logged in as: {}", username);
    }

    @Then("the user should be logged in successfully")
    public void user_should_be_logged_in_successfully() {
        assertTrue(loginPage.isLoggedIn(), "User was not logged in — login may have failed");
        AutomationLogger.info("Login successful");
    }

    @Then("the user should be redirected to the dashboard")
    public void user_redirected_to_dashboard() {
        String currentUrl = driver().getCurrentUrl();
        assertFalse(currentUrl.contains("/login"),
                "Still on login page, not redirected. URL: " + currentUrl);
        AutomationLogger.info("Redirected to: {}", currentUrl);
    }

    // ── Failed login ─────────────────────────────────────────────────────────

    @When("the user logs in with invalid credentials from test data {string}")
    public void user_logs_in_with_invalid_credentials(String dataFile) {
        List<UserTestData> users = TestDataLoader.loadArrayAs("login/" + dataFile, UserTestData.class);
        assertFalse(users.isEmpty(), "No users found in: " + dataFile);
        lastUser = users.get(0);
        loginPage.login(lastUser.getUsername(), lastUser.getPassword());
    }

    @Then("an error message {string} should be displayed")
    public void error_message_should_be_displayed(String expectedMessage) {
        lastErrorMessage = loginPage.getErrorMessage();
        assertNotNull(lastErrorMessage,
                "Expected error message but none was found on the page");
        assertTrue(lastErrorMessage.contains(expectedMessage)
                        || lastErrorMessage.toLowerCase().contains(expectedMessage.toLowerCase()),
                "Unexpected error message. Expected to contain: [" + expectedMessage
                        + "], got: [" + lastErrorMessage + "]");
        AutomationLogger.info("Expected error shown: {}", lastErrorMessage);
    }

    @Then("the user should remain on the login page")
    public void user_should_remain_on_login_page() {
        String url = driver().getCurrentUrl();
        assertTrue(url.contains("/login"),
                "User was redirected away from login page. URL: " + url);
        AutomationLogger.info("User still on login page: {}", url);
    }

    // ── UI element presence ─────────────────────────────────────────────────

    @Then("the login form should be visible")
    public void login_form_should_be_visible() {
        assertTrue(loginPage.isLoginFormVisible(), "Login form is not visible");
    }

    @Then("the username field should be present")
    public void username_field_should_be_present() {
        assertNotNull(loginPage.usernameField(), "Username field not found");
    }
}
