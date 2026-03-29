package com.company.automation.core.wait;

import com.company.automation.core.exception.AutomationException;
import com.company.automation.core.logging.AutomationLogger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Factory for WebDriverWait and FluentWait instances.
 * All explicit waits in the framework should come from here.
 */
public final class WaitFactory {

    private WaitFactory() {}

    // ── WebDriverWait ─────────────────────────────────────────────────────────

    /**
     * Standard explicit wait with the configured default timeout.
     */
    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(WaitConfig.defaultTimeout()));
    }

    /**
     * Explicit wait with a custom timeout in seconds.
     */
    public static WebDriverWait wait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // ── FluentWait ────────────────────────────────────────────────────────────

    /**
     * FluentWait with default polling interval.
     */
    public static FluentWait<WebDriver> fluentWait(WebDriver driver) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(WaitConfig.defaultTimeout()))
                .pollingEvery(Duration.ofMillis(WaitConfig.pollingMs()));
    }

    /**
     * FluentWait with custom timeout and polling.
     */
    public static FluentWait<WebDriver> fluentWait(WebDriver driver, int timeoutSeconds, int pollingMs) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(pollingMs));
    }

    // ── AJAX-ready wait ───────────────────────────────────────────────────────

    /**
     * Waits until jQuery (if present) has no active requests.
     * Uses WebDriverWait under the hood.
     */
    public static void waitForAjax(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(WaitConfig.ajaxTimeout()))
                    .until(d -> {
                        JavascriptExecutor js = (JavascriptExecutor) d;
                        Boolean idle = (Boolean) js.executeScript(
                                "return (typeof jQuery === 'undefined') || (jQuery.active === 0)");
                        return Boolean.TRUE.equals(idle);
                    });
        } catch (Exception e) {
            AutomationLogger.warn("waitForAjax: jQuery check failed or timed out — continuing: {}", e.getMessage());
        }
    }

    // ── Page-load wait ────────────────────────────────────────────────────────

    /**
     * Waits until the page document is in 'complete' state.
     */
    public static void waitForPageLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(WaitConfig.defaultTimeout()))
                    .until(d -> {
                        JavascriptExecutor js = (JavascriptExecutor) d;
                        String state = (String) js.executeScript("return document.readyState");
                        return "complete".equals(state);
                    });
        } catch (Exception e) {
            throw new AutomationException("Page did not reach 'complete' state", e);
        }
    }

    // ── Generic retry wrapper ─────────────────────────────────────────────────

    /**
     * Retries a supplier up to maxAttempts, swallowing stale-element exceptions.
     * Use for actions that may race with dynamic DOM updates.
     *
     * @param action   code to execute
     * @param maxAttempts how many times to retry
     * @param <T>      return type
     * @return the result of the action
     */
    public static <T> T withRetry(java.util.function.Supplier<T> action, int maxAttempts) {
        int attempt = 0;
        while (true) {
            try {
                return action.get();
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                AutomationLogger.debug("Stale element — retry {}/{}", attempt, maxAttempts);
                sleepQuietly(300L);
            }
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
