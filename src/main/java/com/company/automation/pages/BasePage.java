package com.company.automation.pages;

import com.company.automation.core.actions.BrowserActions;
import com.company.automation.core.exception.ElementNotFoundException;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConfig;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base page object — injected with WebDriver via constructor.
 * Provides reusable common UI actions (type, click, clear, getText, wait).
 * Subclasses inherit these; they should NOT contain business assertions here.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final BrowserActions browser;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.browser = new BrowserActions(driver);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ELEMENT FINDERS WITH EXPLICIT WAIT
    // ══════════════════════════════════════════════════════════════════════════

    protected WebElement find(By locator) {
        return find(locator, WaitConfig.defaultTimeout());
    }

    protected WebElement find(By locator, int timeoutSeconds) {
        try {
            return WaitFactory.wait(driver, timeoutSeconds)
                    .until(WaitConditions.visible(locator));
        } catch (Exception e) {
            throw new ElementNotFoundException(locator.toString(), e);
        }
    }

    protected WebElement findPresent(By locator) {
        return findPresent(locator, WaitConfig.defaultTimeout());
    }

    protected WebElement findPresent(By locator, int timeoutSeconds) {
        try {
            return WaitFactory.wait(driver, timeoutSeconds)
                    .until(WaitConditions.present(locator));
        } catch (Exception e) {
            throw new ElementNotFoundException(locator.toString(), e);
        }
    }

    protected WebElement findClickable(By locator) {
        return findClickable(locator, WaitConfig.defaultTimeout());
    }

    protected WebElement findClickable(By locator, int timeoutSeconds) {
        try {
            return WaitFactory.wait(driver, timeoutSeconds)
                    .until(WaitConditions.clickable(locator));
        } catch (Exception e) {
            throw new ElementNotFoundException(locator.toString(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMMON UI ACTIONS
    // ══════════════════════════════════════════════════════════════════════════

    /** Types text after clearing the field first. */
    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
        AutomationLogger.debug("Typed '{}' into {}", text, locator);
    }

    /** Types and submits by pressing Enter. */
    protected void typeAndEnter(By locator, String text) {
        type(locator, text);
        find(locator).sendKeys(Keys.ENTER);
    }

    /** Clicks an element after waiting for it to be clickable. */
    protected void click(By locator) {
        WebElement el = findClickable(locator);
        el.click();
        AutomationLogger.debug("Clicked: {}", locator);
    }

    /** Hovers over an element then clicks the target. */
    protected void hoverAndClick(By hoverLocator, By targetLocator) {
        WebElement target = findClickable(targetLocator);
        new org.openqa.selenium.interactions.Actions(driver)
                .moveToElement(target)
                .click()
                .build()
                .perform();
    }

    /** Returns the visible text of an element. */
    protected String getText(By locator) {
        return find(locator).getText().trim();
    }

    /** Returns an element attribute value. */
    protected String getAttribute(By locator, String attribute) {
        return find(locator).getAttribute(attribute);
    }

    /** Checks if an element is currently displayed. */
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Waits for an element to disappear from the DOM. */
    protected void waitForInvisibility(By locator) {
        WaitFactory.wait(driver).until(WaitConditions.invisible(locator));
    }

    /** Waits for an element to appear. */
    protected void waitForVisibility(By locator) {
        WaitFactory.wait(driver).until(WaitConditions.visible(locator));
    }

    /** Scrolls element into view and waits for it to be visible. */
    protected void scrollAndFind(By locator) {
        WebElement el = findPresent(locator);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        WaitFactory.wait(driver).until(WaitConditions.visible(el));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ABSTRACT — subclasses must define
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Subclasses implement this to verify the page loaded correctly.
     * Called after construction to confirm the page is ready.
     */
    public abstract boolean isLoaded();

    // ══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    protected void openUrl(String url) {
        driver.get(url);
        WaitFactory.waitForPageLoad(driver);
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected WebDriverWait waitFor() {
        return WaitFactory.wait(driver);
    }
}
