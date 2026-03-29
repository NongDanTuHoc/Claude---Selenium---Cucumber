package com.company.automation.hooks;

import com.company.automation.core.driver.DriverFactory;
import com.company.automation.core.driver.DriverManager;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.reporting.ReportConfig;
import com.company.automation.reporting.ScreenshotManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber scenario lifecycle hooks.
 * Each @Before creates a fresh WebDriver per scenario thread.
 * Each @After quits the driver and captures a screenshot on failure.
 */
public class CucumberHooks {

    @Before(order = 1)
    public void beforeScenario(Scenario scenario) {
        AutomationLogger.info("━━━ SCENARIO START: {} ━━━", scenario.getName());
        WebDriver driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                AutomationLogger.error("SCENARIO FAILED: {}", scenario.getName());
                var screenshotPath = ScreenshotManager.captureOnFailure(scenario.getName());

                // Attach screenshot to ExtentReports if available
                try {
                    var extentTest = ReportConfig.createTest(scenario.getName());
                    extentTest.fail("Scenario failed: " + scenario.getName());
                    if (screenshotPath != null) {
                        extentTest.addScreenCaptureFromPath(screenshotPath.toString());
                    }
                } catch (Exception e) {
                    AutomationLogger.warn("ExtentReports attachment failed: {}", e.getMessage());
                }

                // Also attach as Cucumber embed for the JSON report
                try {
                    String base64 = ScreenshotManager.captureAsBase64();
                    if (!base64.isEmpty()) {
                        scenario.attach(
                                java.util.Base64.getDecoder().decode(base64),
                                "image/png",
                                "failure-screenshot");
                    }
                } catch (Exception ignored) {}
            } else {
                AutomationLogger.info("SCENARIO PASSED: {}", scenario.getName());
            }
        } finally {
            DriverManager.quitDriver();
            AutomationLogger.info("━━━ SCENARIO END ━━━");
        }
    }
}
