package com.company.automation.core.actions;

import com.company.automation.core.exception.AutomationException;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConfig;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared browser-level actions available to pages and components.
 * All waits are explicit — no Thread.sleep.
 * Retries once on StaleElementReferenceException to handle dynamic DOM updates.
 */
public class BrowserActions {

    protected final WebDriver driver;

    public BrowserActions(WebDriver driver) {
        this.driver = driver;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    public void openUrl(String url) {
        driver.get(url);
        WaitFactory.waitForPageLoad(driver);
        AutomationLogger.info("Navigated to: {}", url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void refresh() {
        driver.navigate().refresh();
        WaitFactory.waitForPageLoad(driver);
    }

    public void navigateBack() {
        driver.navigate().back();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FRAMES / IFRAMES
    // ══════════════════════════════════════════════════════════════════════════

    public void switchToFrame(By frameLocator) {
        WebDriverWait wait = WaitFactory.wait(driver);
        WebElement frameEl = wait.until(WaitConditions.present(frameLocator));
        driver.switchTo().frame(frameEl);
    }

    public void switchToFrame(WebElement frameElement) {
        driver.switchTo().frame(frameElement);
    }

    public void switchToFrameByIndex(int index) {
        driver.switchTo().frame(index);
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public void switchToAlert() {
        WaitFactory.wait(driver, 5).until(
                d -> d.switchTo().alert() != null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WINDOWS / TABS
    // ══════════════════════════════════════════════════════════════════════════

    public void switchToWindow(int windowIndex) {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (windowIndex < handles.size()) {
            driver.switchTo().window(handles.get(windowIndex));
        } else {
            throw new AutomationException(
                    "Window index " + windowIndex + " out of bounds. Available: " + handles.size());
        }
    }

    public void switchToNewWindow() {
        String mainWindow = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(mainWindow)) {
                driver.switchTo().window(h);
                return;
            }
        }
        throw new AutomationException("No new window found");
    }

    public void closeCurrentWindow() {
        driver.close();
    }

    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ALERT
    // ══════════════════════════════════════════════════════════════════════════

    public void acceptAlert() {
        Alert alert = WaitFactory.wait(driver, 5).until(
                d -> { try { return d.switchTo().alert(); } catch (Exception e) { return null; } });
        alert.accept();
    }

    public void dismissAlert() {
        Alert alert = WaitFactory.wait(driver, 5).until(
                d -> { try { return d.switchTo().alert(); } catch (Exception e) { return null; } });
        alert.dismiss();
    }

    public String getAlertText() {
        Alert alert = WaitFactory.wait(driver, 5).until(
                d -> { try { return d.switchTo().alert(); } catch (Exception e) { return null; } });
        return alert.getText();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HOVER / HOVER & CLICK
    // ══════════════════════════════════════════════════════════════════════════

    public void hover(By locator) {
        WebElement el = WaitFactory.wait(driver).until(WaitConditions.visible(locator));
        hover(el);
    }

    public void hover(WebElement element) {
        new Actions(driver).moveToElement(element).perform();
    }

    public void hoverAndClick(By targetLocator) {
        WebElement target = WaitFactory.wait(driver).until(WaitConditions.visible(targetLocator));
        new Actions(driver).moveToElement(target).click().build().perform();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SCROLL
    // ══════════════════════════════════════════════════════════════════════════

    public void scrollIntoView(By locator) {
        WebElement el = driver.findElement(locator);
        scrollIntoView(el);
    }

    public void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, 0);");
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JAVASCRIPT EXECUTION
    // ══════════════════════════════════════════════════════════════════════════

    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public void clickViaJs(By locator) {
        WebElement el = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    public void clickViaJs(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ELEMENT STATE
    // ══════════════════════════════════════════════════════════════════════════

    public boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        try {
            return driver.findElement(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSelected(By locator) {
        try {
            return driver.findElement(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SELECT (dropdown)
    // ══════════════════════════════════════════════════════════════════════════

    public void selectByVisibleText(By dropdownLocator, String text) {
        WebElement el = WaitFactory.wait(driver).until(WaitConditions.visible(dropdownLocator));
        new Select(el).selectByVisibleText(text);
    }

    public void selectByValue(By dropdownLocator, String value) {
        WebElement el = WaitFactory.wait(driver).until(WaitConditions.visible(dropdownLocator));
        new Select(el).selectByValue(value);
    }

    public void selectByIndex(By dropdownLocator, int index) {
        WebElement el = WaitFactory.wait(driver).until(WaitConditions.visible(dropdownLocator));
        new Select(el).selectByIndex(index);
    }

    public String getSelectedText(By dropdownLocator) {
        WebElement el = WaitFactory.wait(driver).until(WaitConditions.visible(dropdownLocator));
        return new Select(el).getFirstSelectedOption().getText();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RETRY CLICK (for stale element cases)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Clicks an element with one automatic retry on StaleElementReferenceException.
     */
    public void clickWithRetry(By locator) {
        clickWithRetry(driver.findElement(locator));
    }

    public void clickWithRetry(WebElement element) {
        try {
            element.click();
        } catch (StaleElementReferenceException e) {
            AutomationLogger.debug("StaleElement — retrying click on: {}", element);
            sleep(200);
            element.click();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    protected WebDriverWait waitFor() {
        return WaitFactory.wait(driver);
    }

    protected WebDriverWait wait(int seconds) {
        return WaitFactory.wait(driver, seconds);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
