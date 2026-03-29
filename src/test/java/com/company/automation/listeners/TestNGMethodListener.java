package com.company.automation.listeners;

import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.reporting.ReportConfig;
import com.company.automation.reporting.ScreenshotManager;
import com.aventstack.extentreports.ExtentTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ITestListener — runs per @Test method in TestNG suites.
 * Logs start / pass / fail / skip and attaches failure screenshots to ExtentReports.
 */
public class TestNGMethodListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> TEST_NODE = new ThreadLocal<>();

    // ── ITestListener ───────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        String name = fullName(result);
        AutomationLogger.info("[TEST START] {}", name);
        ExtentTest node = ReportConfig.createTest(name, descriptionOf(result));
        TEST_NODE.set(node);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String name = fullName(result);
        AutomationLogger.info("[TEST PASS] {}", name);
        ExtentTest node = TEST_NODE.get();
        if (node != null) {
            node.pass("Test passed");
            TEST_NODE.remove();
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = fullName(result);
        AutomationLogger.error("[TEST FAIL] {}", name);

        // Capture screenshot
        var screenshotPath = ScreenshotManager.captureOnFailure(name);
        String screenshotNote = screenshotPath != null
                ? "Screenshot: " + screenshotPath.getFileName()
                : "Screenshot unavailable";

        ExtentTest node = TEST_NODE.get();
        if (node != null) {
            node.fail("Test failed: " + getReason(result) + "\n" + screenshotNote);
            if (screenshotPath != null) {
                node.addScreenCaptureFromPath(screenshotPath.toString());
            }
            TEST_NODE.remove();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String name = fullName(result);
        AutomationLogger.warn("[TEST SKIP] {}", name);
        ExtentTest node = TEST_NODE.get();
        if (node != null) {
            node.skip("Test skipped: " + getReason(result));
            TEST_NODE.remove();
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Flaky / retry scenario
        String name = fullName(result);
        AutomationLogger.warn("[TEST FLAKY] {} — within success %", name);
        ExtentTest node = TEST_NODE.get();
        if (node != null) {
            node.warning("Test failed but within success percentage threshold");
        }
    }

    @Override
    public void onStart(ITestContext context) {
        AutomationLogger.info("━━━ TEST CONTEXT START: {} ━━━", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        AutomationLogger.info("━━━ TEST CONTEXT FINISH: {} ━━━", context.getName());
        ReportConfig.flush();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String fullName(ITestResult r) {
        return r.getTestClass().getName() + "." + r.getMethod().getMethodName();
    }

    private String descriptionOf(ITestResult r) {
        String desc = r.getMethod().getDescription();
        return (desc == null || desc.isBlank()) ? "" : desc;
    }

    private String getReason(ITestResult r) {
        Throwable t = r.getThrowable();
        if (t == null) return "Unknown";
        String msg = t.getMessage();
        return (msg == null || msg.isBlank()) ? t.getClass().getSimpleName() : msg;
    }
}
