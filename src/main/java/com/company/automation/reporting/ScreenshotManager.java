package com.company.automation.reporting;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;
import com.company.automation.core.driver.DriverManager;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.utilities.FileUtil;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Captures and saves PNG screenshots to the reports/ directory.
 * Automatically captures on test failure when used with CucumberHooks / TestNGMethodListener.
 */
public final class ScreenshotManager {

    private static final String SCREENSHOT_DIR = "reports/test-output/screenshots";
    private static final SimpleDateFormat STAMP = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

    private ScreenshotManager() {}

    // ── Capture ────────────────────────────────────────────────────────────

    /**
     * Captures a full-page screenshot and saves to reports/test-output/screenshots/.
     *
     * @param name  screenshot name prefix (e.g. "login_page_failure")
     * @return absolute path of the saved file, or null if capture failed
     */
    public static Path capture(String name) {
        return capture(DriverManager.getDriver(), name);
    }

    /**
     * Captures a screenshot using an explicit WebDriver instance.
     */
    public static Path capture(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot)) {
            AutomationLogger.warn("WebDriver does not implement TakesScreenshot — screenshot skipped");
            return null;
        }

        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = buildPath(name, "png");
            FileUtils.copyFile(src, dest.toFile());
            AutomationLogger.info("Screenshot saved: {}", dest);
            return dest;
        } catch (Exception e) {
            AutomationLogger.warn("Screenshot capture failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot and returns it as a Base64-encoded String (for embedding in HTML reports).
     */
    public static String captureAsBase64() {
        return captureAsBase64(DriverManager.getDriver());
    }

    public static String captureAsBase64(WebDriver driver) {
        if (!(driver instanceof TakesScreenshot)) {
            return "";
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            AutomationLogger.warn("Base64 screenshot failed: {}", e.getMessage());
            return "";
        }
    }

    // ── Auto-failure capture ──────────────────────────────────────────────

    /**
     * Captures a screenshot named with the current timestamp and test context.
     * Designed for @AfterMethod / CucumberHooks @After hooks.
     *
     * @param scenarioName  current scenario or test method name
     * @return path to the saved screenshot, or null
     */
    public static Path captureOnFailure(String scenarioName) {
        if (!ConfigLoader.getBool(ConfigKeys.REPORT_SCREENSHOT, true)) {
            return null;
        }
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = "FAIL_" + safeName + "_" + STAMP.format(new Date());
        return capture(fileName);
    }

    // ── Path helpers ───────────────────────────────────────────────────────

    /**
     * Builds the absolute destination path for a screenshot.
     * Creates parent directories as needed.
     */
    public static Path buildPath(String name, String extension) {
        String dir = ConfigLoader.get(ConfigKeys.REPORT_PATH, SCREENSHOT_DIR);
        Path dirPath = Paths.get(dir).toAbsolutePath().normalize();
        FileUtil.ensureDir(dirPath);

        String fileName = sanitise(name) + "_" + STAMP.format(new Date()) + "." + extension;
        return dirPath.resolve(fileName);
    }

    /**
     * Returns the screenshot directory as a Path.
     */
    public static Path getScreenshotDir() {
        String dir = ConfigLoader.get(ConfigKeys.REPORT_PATH, SCREENSHOT_DIR);
        Path dirPath = Paths.get(dir).toAbsolutePath().normalize();
        FileUtil.ensureDir(dirPath);
        return dirPath;
    }

    // ── Private ───────────────────────────────────────────────────────────

    private static String sanitise(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").replaceAll("_+", "_");
    }
}
