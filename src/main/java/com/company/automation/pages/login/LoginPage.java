package com.company.automation.pages.login;

import com.company.automation.core.wait.WaitConfig;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import com.company.automation.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Login page object — maps the /login page UI elements and actions.
 * Extends BasePage; WebDriver injected via constructor.
 */
public class LoginPage extends BasePage {

    // ── Locators ────────────────────────────────────────────────────────────

    private final By usernameField    = By.id("username");
    private final By passwordField    = By.id("password");
    private final By loginButton      = By.id("login-btn");
    private final By errorMessage     = By.cssSelector(".alert-danger, .error-message, [role='alert']");
    private final By successMessage   = By.cssSelector(".alert-success, .success-message");
    private final By logoutButton     = By.id("logout-btn");
    private final By loginForm        = By.id("login-form");
    private final By forgotPassword   = By.linkText("Forgot password?");
    private final By rememberMe       = By.id("remember-me");

    // ── Constructor ──────────────────────────────────────────────────────────

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ── Page load check ─────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isDisplayed(loginButton) && isDisplayed(usernameField);
    }

    // ── Login actions ───────────────────────────────────────────────────────

    /**
     * Performs a full login — clears fields, types credentials, clicks login.
     */
    public void login(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
        WaitFactory.waitForPageLoad(driver);
    }

    /**
     * Login with "Remember Me" checkbox ticked.
     */
    public void loginWithRememberMe(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(rememberMe);
        click(loginButton);
        WaitFactory.waitForPageLoad(driver);
    }

    // ── Logout ─────────────────────────────────────────────────────────────

    /** Returns true if the logout button is visible (indicates logged-in state). */
    public boolean isLoggedIn() {
        return isDisplayed(logoutButton);
    }

    public void logout() {
        click(logoutButton);
    }

    // ── Error / feedback helpers ──────────────────────────────────────────

    /** Returns the error message text shown on failed login, or empty string. */
    public String getErrorMessage() {
        try {
            WebElement el = WaitFactory.wait(driver, 3)
                    .until(WaitConditions.visible(errorMessage));
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Returns the success message text, or empty string. */
    public String getSuccessMessage() {
        try {
            WebElement el = WaitFactory.wait(driver, 3)
                    .until(WaitConditions.visible(successMessage));
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ── Element accessors (for step-defs that need raw elements) ────────────

    public WebElement usernameField() {
        return find(usernameField);
    }

    public WebElement passwordField() {
        return find(passwordField);
    }

    public boolean isLoginFormVisible() {
        return isDisplayed(loginForm);
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    public void openForgotPassword() {
        click(forgotPassword);
    }
}
