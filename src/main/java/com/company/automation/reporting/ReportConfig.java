package com.company.automation.reporting;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;
import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ExtentReports singleton — initialised once per test run.
 * Generates HTML report to reports/extent/ by default.
 *
 * Usage:
 * <pre>
 * ExtentReports extent = ReportConfig.getInstance();
 * ExtentTest test = extent.createTest("Login");
 * test.pass("User logged in successfully");
 * extent.flush();
 * </pre>
 */
public final class ReportConfig {

    private static ExtentReports instance;
    private static final String REPORT_DIR = "reports/extent";

    private ReportConfig() {}

    // ── Singleton ───────────────────────────────────────────────────────────

    /**
     * Returns the singleton ExtentReports instance, creating it if necessary.
     */
    public static synchronized ExtentReports getInstance() {
        if (instance == null) {
            instance = create();
        }
        return instance;
    }

    /**
     * Resets the singleton — call between suite runs.
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.flush();
            instance = null;
        }
    }

    // ── Create ─────────────────────────────────────────────────────────────

    private static ExtentReports create() {
        String reportDir = ConfigLoader.get(ConfigKeys.REPORT_PATH, REPORT_DIR);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path reportDirPath = Paths.get(reportDir).toAbsolutePath().normalize();
        Path htmlFile = reportDirPath.resolve("TestReport_" + timestamp + ".html");

        // Ensure directory exists
        reportDirPath.toFile().mkdirs();

        // Configure the spark reporter via its config object
        ExtentSparkReporter spark = new ExtentSparkReporter(htmlFile.toFile());
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Automation Test Report");
        spark.config().setReportName("Automation Framework Report");
        spark.config().setTimelineEnabled(true);

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setAnalysisStrategy(AnalysisStrategy.TEST);

        extent.setSystemInfo("Environment", ConfigLoader.get(ConfigKeys.ENV, "dev"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Platform", System.getProperty("os.name"));

        AutomationLogger.info("ExtentReports initialised: {}", htmlFile);
        return extent;
    }

    // ── Test node factory ─────────────────────────────────────────────────

    /**
     * Creates and returns a top-level test node for the given name.
     */
    public static ExtentTest createTest(String testName) {
        return getInstance().createTest(testName);
    }

    /**
     * Creates a test node as a child of a parent test.
     */
    public static ExtentTest createTest(String testName, String description) {
        return getInstance().createTest(testName, description);
    }

    /**
     * Flushes and writes the report to disk.
     */
    public static void flush() {
        if (instance != null) {
            instance.flush();
            AutomationLogger.info("ExtentReports flushed to disk");
        }
    }

    /** Returns the reports directory path. */
    public static Path getReportDir() {
        return Paths.get(ConfigLoader.get(ConfigKeys.REPORT_PATH, REPORT_DIR))
                .toAbsolutePath().normalize();
    }
}
