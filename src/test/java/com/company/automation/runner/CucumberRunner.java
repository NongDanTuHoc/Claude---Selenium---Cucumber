package com.company.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Cucumber JVM + TestNG integration runner.
 *
 * Runs all .feature files under src/test/resources/features/.
 * Uses the @CucumberContextConfiguration pattern for TestNG integration.
 *
 * Execution:
 *   mvn test -Dcucumber.filter.tags="@smoke"
 *   mvn test -Denv=staging
 *   mvn test -Denv=prod -Dcucumber.filter.tags="@regression"
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.company.automation.hooks",
                "com.company.automation.stepdefinitions"
        },
        plugin = {
                "html:reports/cucumber/cucumber_report.html",
                "json:reports/cucumber/cucumber_report.json",
                "junit:reports/cucumber/cucumber_report.xml",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "",
        publish = false
)
@Test
public class CucumberRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
