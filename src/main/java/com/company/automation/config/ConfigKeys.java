package com.company.automation.config;

/**
 * Centralized configuration property keys.
 * All keys must match entries in config/env/config.*.properties files.
 */
public final class ConfigKeys {

    private ConfigKeys() {}

    // ── Application ──────────────────────────────────────────────────────────
    public static final String APP_BASE_URL       = "app.baseurl";
    public static final String APP_TIMEOUT         = "app.timeout";
    public static final String APP_IMPLICIT_WAIT   = "app.implicitwait";

    // ── Driver / Selenium ────────────────────────────────────────────────────
    public static final String DRIVER_TYPE         = "driver.type";
    public static final String DRIVER_HEADLESS     = "driver.headless";
    public static final String DRIVER_MAXIMIZE     = "driver.window.maximize";
    public static final String DRIVER_REMOTE       = "driver.remote.enabled";
    public static final String DRIVER_REMOTE_URL   = "driver.remote.url";

    // ── Database ─────────────────────────────────────────────────────────────
    public static final String DB_ENABLED          = "db.enabled";
    public static final String DB_TYPE             = "db.type";
    public static final String DB_HOST             = "db.host";
    public static final String DB_PORT             = "db.port";
    public static final String DB_NAME             = "db.name";
    public static final String DB_USERNAME         = "db.username";
    public static final String DB_PASSWORD         = "db.password";

    // ── API ──────────────────────────────────────────────────────────────────
    public static final String API_BASE_URL        = "api.baseurl";
    public static final String API_TIMEOUT         = "api.timeout";

    // ── Reporting ───────────────────────────────────────────────────────────
    public static final String REPORT_SCREENSHOT   = "report.screenshot.onfailure";
    public static final String REPORT_PATH         = "report.path";
    public static final String REPORT_HTML_ENABLED = "report.html.enabled";

    // ── Logging ─────────────────────────────────────────────────────────────
    public static final String LOG_LEVEL           = "log.level";
    public static final String LOG_PATTERN         = "log.pattern";

    // ── Env flag ─────────────────────────────────────────────────────────────
    public static final String ENV                 = "env";
}
