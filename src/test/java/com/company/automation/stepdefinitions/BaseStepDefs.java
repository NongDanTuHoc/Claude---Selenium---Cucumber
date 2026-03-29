package com.company.automation.stepdefinitions;

import com.company.automation.core.driver.DriverManager;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.pages.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * Base step definition class — provides shared WebDriver access to all step-def packages.
 * Subclasses use this.driver() to get the current thread's WebDriver instance.
 */
public abstract class BaseStepDefs {

    private WebDriver driver;

    protected WebDriver driver() {
        if (driver == null) {
            driver = DriverManager.getDriver();
        }
        return driver;
    }

    /**
     * Convenience — opens the application base URL.
     */
    protected void openBaseUrl() {
        DriverManager.openBaseUrl();
    }

    /**
     * Navigates to an explicit URL.
     */
    protected void openUrl(String url) {
        driver().navigate().to(url);
    }

    /**
     * Waits for page load after navigation.
     */
    protected void waitForPageLoad() {
        com.company.automation.core.wait.WaitFactory.waitForPageLoad(driver());
    }

    /**
     * Delegates to the shared AutomationLogger for step-level logging.
     */
    protected void logInfo(String message, Object... args) {
        AutomationLogger.info(message, args);
    }
}
