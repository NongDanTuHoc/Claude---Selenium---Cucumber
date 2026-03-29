package com.company.automation.core.driver;

import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe WebDriver registry using ThreadLocal storage.
 * Each test thread gets its own isolated driver instance.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER_THREAD = new ThreadLocal<>();
    private static final Map<Long, DriverType>  DRIVER_TYPES  = new HashMap<>();

    private DriverManager() {}

    // ── Thread-local accessors ───────────────────────────────────────────────

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER_THREAD.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver has not been initialised for this thread. "
                    + "Call DriverFactory.createDriver() first.");
        }
        return driver;
    }

    public static void setDriver(WebDriver driver) {
        DRIVER_THREAD.set(driver);
        DRIVER_TYPES.put(Thread.currentThread().threadId(), DriverFactory.currentType());
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER_THREAD.get();
        if (driver != null) {
            try {
                driver.quit();
                AutomationLogger.info("WebDriver quit for thread: {}", Thread.currentThread().threadId());
            } catch (Exception e) {
                AutomationLogger.warn("Error quitting WebDriver: {}", e.getMessage());
            } finally {
                DRIVER_THREAD.remove();
                DRIVER_TYPES.remove(Thread.currentThread().threadId());
            }
        }
    }

    public static boolean hasDriver() {
        return DRIVER_THREAD.get() != null;
    }

    public static DriverType getDriverType() {
        return DRIVER_TYPES.getOrDefault(Thread.currentThread().threadId(), DriverType.CHROME);
    }

    /**
     * Maximises and navigates to base URL as a convenience.
     */
    public static void openBaseUrl() {
        String url = ConfigLoader.get("app.baseurl", "about:blank");
        WebDriver driver = getDriver();
        driver.manage().window().maximize();
        driver.get(url);
        AutomationLogger.info("Opened base URL: {}", url);
    }

    /** Clean up all drivers (call in AfterAll / after suite). */
    public static void closeAll() {
        DRIVER_THREAD.remove();
        AutomationLogger.info("All WebDriver instances cleaned up");
    }
}
