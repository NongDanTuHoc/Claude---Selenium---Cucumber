package com.company.automation.listeners;

import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.reporting.ReportConfig;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * ISuiteListener — runs once per TestNG suite (XML file or class suite).
 * Opens ExtentReports at suite start and flushes at suite end.
 */
public class TestNGSuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        AutomationLogger.info("═══ SUITE START: {} ═══", suite.getName());
        ReportConfig.reset();      // fresh report per suite
        ReportConfig.getInstance(); // triggers singleton init
    }

    @Override
    public void onFinish(ISuite suite) {
        AutomationLogger.info("═══ SUITE FINISH: {} ═══", suite.getName());
        ReportConfig.flush();
    }
}
