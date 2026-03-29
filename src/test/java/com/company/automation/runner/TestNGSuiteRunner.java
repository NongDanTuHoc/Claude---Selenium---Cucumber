package com.company.automation.runner;

import com.company.automation.listeners.TestNGMethodListener;
import com.company.automation.listeners.TestNGSuiteListener;
import org.testng.annotations.Listeners;

/**
 * TestNG XML suite runner.
 * This class is NOT directly executed — it acts as the reference class for XML suites.
 * XML suites point to this class via:
 *   <classes><class name="com.company.automation.runner.TestNGSuiteRunner"/></classes>
 *
 * Listeners are declared here so all tests in the suite inherit them automatically.
 *
 * Execution:
 *   mvn test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
 *   mvn test -DsuiteXmlFile=src/test/resources/suites/regression.xml -Denv=staging
 */
@Listeners({
        TestNGSuiteListener.class,
        TestNGMethodListener.class
})
public class TestNGSuiteRunner {
    // Marker class — XML suites reference it to load the listeners.
    // Actual test methods live in page / API test classes annotated with @Test.
}
