package com.company.automation.core.driver;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Factory for creating WebDriver instances (local and remote/grid).
 * Driver type is determined by the active config profile.
 */
public final class DriverFactory {

    private DriverFactory() {}

    // ── Entry point ──────────────────────────────────────────────────────────

    public static WebDriver createDriver() {
        boolean remoteEnabled = ConfigLoader.getBool(ConfigKeys.DRIVER_REMOTE, false);
        String remoteUrl      = ConfigLoader.get(ConfigKeys.DRIVER_REMOTE_URL, "");

        DriverType driverType;
        if (remoteEnabled && !remoteUrl.isBlank()) {
            driverType = DriverType.REMOTE;
        } else {
            driverType = DriverType.fromString(ConfigLoader.get(ConfigKeys.DRIVER_TYPE, "chrome"));
        }

        return createDriver(driverType, remoteUrl);
    }

    // ── Per-type factory ─────────────────────────────────────────────────────

    public static WebDriver createDriver(DriverType type, String remoteUrl) {
        WebDriver driver;

        switch (type) {
            case CHROME:
                driver = createChrome();
                break;
            case FIREFOX:
                driver = createFirefox();
                break;
            case EDGE:
                driver = createEdge();
                break;
            case REMOTE:
                driver = createRemote(remoteUrl);
                break;
            default:
                throw new IllegalArgumentException("Unsupported driver type: " + type);
        }

        AutomationLogger.info("WebDriver created: {} (remote={})", type, type == DriverType.REMOTE);
        return driver;
    }

    /** Returns the type of driver most recently created on this thread. */
    public static DriverType currentType() {
        return DriverManager.getDriverType();
    }

    // ── Local browser factories ───────────────────────────────────────────────

    private static WebDriver createChrome() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = buildChromeOptions();
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = buildFirefoxOptions();
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdge() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = buildEdgeOptions();
        return new EdgeDriver(options);
    }

    private static WebDriver createRemote(String remoteUrl) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        String browserName = ConfigLoader.get(ConfigKeys.DRIVER_TYPE, "chrome");
        capabilities.setBrowserName(browserName);
        try {
            return new RemoteWebDriver(URI.create(remoteUrl).toURL(), capabilities);
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid Selenium Grid URL: " + remoteUrl, e);
        }
    }

    // ── Options builders ─────────────────────────────────────────────────────

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions opts = new ChromeOptions();
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_HEADLESS, false)) {
            opts.addArguments("--headless=new");
        }
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_MAXIMIZE, true)) {
            opts.addArguments("--start-maximized");
        }
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--disable-gpu");
        opts.addArguments("--ignore-certificate-errors");
        opts.addArguments("--lang=en-US");
        return opts;
    }

    private static FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions opts = new FirefoxOptions();
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_HEADLESS, false)) {
            opts.addArguments("-headless");
        }
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_MAXIMIZE, true)) {
            opts.addArguments("--width=1920", "--height=1080");
        }
        opts.addArguments("--no-sandbox");
        return opts;
    }

    private static EdgeOptions buildEdgeOptions() {
        EdgeOptions opts = new EdgeOptions();
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_HEADLESS, false)) {
            opts.addArguments("--headless=new");
        }
        if (ConfigLoader.getBool(ConfigKeys.DRIVER_MAXIMIZE, true)) {
            opts.addArguments("--start-maximized");
        }
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-dev-shm-usage");
        return opts;
    }
}
