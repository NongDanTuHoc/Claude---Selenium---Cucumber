package com.company.automation.core.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application-level logger wrapper.
 * All framework classes should use this instead of raw LogManager.
 */
public final class AutomationLogger {

    private static final Logger LOG = LogManager.getLogger("com.company.automation");

    private AutomationLogger() {}

    // ── Standard SLF4J-style methods ─────────────────────────────────────────

    public static void trace(String message) {
        LOG.trace(message);
    }

    public static void trace(String message, Object... args) {
        LOG.trace(message, args);
    }

    public static void debug(String message) {
        LOG.debug(message);
    }

    public static void debug(String message, Object... args) {
        LOG.debug(message, args);
    }

    public static void info(String message) {
        LOG.info(message);
    }

    public static void info(String message, Object... args) {
        LOG.info(message, args);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void warn(String message, Object... args) {
        LOG.warn(message, args);
    }

    public static void warn(String message, Throwable t) {
        LOG.warn(message, t);
    }

    public static void error(String message) {
        LOG.error(message);
    }

    public static void error(String message, Object... args) {
        LOG.error(message, args);
    }

    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }

    public static void fatal(String message) {
        LOG.fatal(message);
    }

    public static void fatal(String message, Throwable t) {
        LOG.fatal(message, t);
    }

    /**
     * Returns the underlying Log4j2 logger for advanced use.
     */
    public static Logger getLogger() {
        return LOG;
    }
}
