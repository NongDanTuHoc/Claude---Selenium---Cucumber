package com.company.automation.core.wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Custom and composed ExpectedConditions for use with WebDriverWait.
 * All conditions delegate to Selenium's built-in ones where possible.
 */
public final class WaitConditions {

    private WaitConditions() {}

    // ── Element conditions ────────────────────────────────────────────────────

    public static ExpectedCondition<WebElement> visible(By locator) {
        return ExpectedConditions.visibilityOfElementLocated(locator);
    }

    public static ExpectedCondition<WebElement> visible(WebElement element) {
        return ExpectedConditions.visibilityOf(element);
    }

    public static ExpectedCondition<WebElement> clickable(By locator) {
        return ExpectedConditions.elementToBeClickable(locator);
    }

    public static ExpectedCondition<WebElement> clickable(WebElement element) {
        return ExpectedConditions.elementToBeClickable(element);
    }

    public static ExpectedCondition<Boolean> invisible(By locator) {
        return ExpectedConditions.invisibilityOfElementLocated(locator);
    }

    public static ExpectedCondition<Boolean> invisible(WebElement element) {
        return ExpectedConditions.invisibilityOf(element);
    }

    public static ExpectedCondition<WebElement> present(By locator) {
        return ExpectedConditions.presenceOfElementLocated(locator);
    }

    public static ExpectedCondition<List<WebElement>> allPresent(By locator) {
        return ExpectedConditions.presenceOfAllElementsLocatedBy(locator);
    }

    public static ExpectedCondition<List<WebElement>> allVisible(By locator) {
        return d -> {
            if (d == null) return null;
            try {
                List<WebElement> els = d.findElements(locator);
                if (els.isEmpty()) return null;
                for (WebElement el : els) {
                    if (!el.isDisplayed()) return null;
                }
                return els;
            } catch (Exception e) {
                return null;
            }
        };
    }

    // ── Text conditions ───────────────────────────────────────────────────────

    public static ExpectedCondition<Boolean> textToBe(By locator, String expectedText) {
        return ExpectedConditions.textToBe(locator, expectedText);
    }

    public static ExpectedCondition<Boolean> textContains(WebElement element, String expectedText) {
        return d -> {
            try {
                return element.getText().contains(expectedText);
            } catch (Exception e) {
                return false;
            }
        };
    }

    public static ExpectedCondition<Boolean> textToBeNonEmpty(By locator) {
        return d -> {
            if (d == null) return false;
            try {
                WebElement el = d.findElement(locator);
                return el != null && !el.getText().trim().isEmpty();
            } catch (Exception e) {
                return false;
            }
        };
    }

    // ── Attribute conditions ─────────────────────────────────────────────────

    public static ExpectedCondition<Boolean> attributeToBe(
            WebElement element, String attribute, String expectedValue) {
        return ExpectedConditions.attributeToBe(element, attribute, expectedValue);
    }

    public static ExpectedCondition<Boolean> attributeContains(
            WebElement element, String attribute, String expectedValue) {
        return ExpectedConditions.attributeContains(element, attribute, expectedValue);
    }

    // ── Element state ─────────────────────────────────────────────────────────

    public static ExpectedCondition<Boolean> selected(WebElement element) {
        return ExpectedConditions.elementToBeSelected(element);
    }

    public static ExpectedCondition<Boolean> notSelected(WebElement element) {
        return d -> !ExpectedConditions.elementToBeSelected(element).apply(d);
    }

    public static ExpectedCondition<Boolean> enabled(WebElement element) {
        return d -> {
            try {
                return element.isEnabled();
            } catch (Exception e) {
                return false;
            }
        };
    }

    // ── Custom / composed ────────────────────────────────────────────────────

    /**
     * Waits for an element to be visible and present in the DOM.
     */
    public static ExpectedCondition<WebElement> visibleAndPresent(By locator) {
        return d -> {
            try {
                WebElement el = d.findElement(locator);
                if (el.isDisplayed()) {
                    return el;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        };
    }

    /**
     * Polls a custom boolean supplier until it returns true or times out.
     */
    public static ExpectedCondition<Boolean> pollUntil(
            java.util.function.Supplier<Boolean> condition, int intervalMs) {
        return d -> {
            try {
                return condition.get();
            } catch (Exception e) {
                return false;
            }
        };
    }
}
