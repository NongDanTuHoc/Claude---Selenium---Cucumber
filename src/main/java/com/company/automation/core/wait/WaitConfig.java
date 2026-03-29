package com.company.automation.core.wait;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;

/**
 * Holds timeout and polling configuration for explicit waits.
 * Values are read once from ConfigLoader at startup.
 */
public final class WaitConfig {

    /** Default timeout in seconds for element visibility, clickability, etc. */
    private static final int DEFAULT_TIMEOUT_SECONDS = ConfigLoader.getInt(
            ConfigKeys.APP_TIMEOUT, 30);

    /** Polling interval in milliseconds for FluentWait. */
    private static final int DEFAULT_POLLING_MS = 500;

    /** Timeout for AJAX / JavaScript-ready checks. */
    private static final int AJAX_TIMEOUT_SECONDS = ConfigLoader.getInt(
            "app.ajax.timeout", 15);

    private WaitConfig() {}

    public static int defaultTimeout() {
        return DEFAULT_TIMEOUT_SECONDS;
    }

    public static int ajaxTimeout() {
        return AJAX_TIMEOUT_SECONDS;
    }

    public static int pollingMs() {
        return DEFAULT_POLLING_MS;
    }
}
