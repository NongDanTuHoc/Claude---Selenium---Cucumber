package com.company.automation.components;

import com.company.automation.core.exception.ElementNotFoundException;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConfig;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Base component — shared parent for page-fragment objects.
 * Components represent a sub-section of a page (e.g. a table, modal, sidebar).
 * WebDriver is injected via constructor (same pattern as pages).
 */
public abstract class BaseComponent {

    protected final WebDriver driver;
    protected final WebElement root;

    /**
     * Main constructor — pass the WebElement that is the root of this fragment.
     */
    public BaseComponent(WebDriver driver, WebElement root) {
        this.driver = driver;
        this.root = root;
    }

    /**
     * No-root constructor — subclasses that use global locators call this.
     * root will be null and finders will search the whole page.
     */
    protected BaseComponent(WebDriver driver) {
        this.driver = driver;
        this.root = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ELEMENT FINDERS — scoped to root if present, otherwise global
    // ══════════════════════════════════════════════════════════════════════════

    protected WebElement findChild(By locator) {
        return findChild(locator, WaitConfig.defaultTimeout());
    }

    protected WebElement findChild(By locator, int timeoutSeconds) {
        try {
            if (root != null) {
                return WaitFactory.wait(driver, timeoutSeconds)
                        .until(d -> {
                            try {
                                WebElement found = root.findElement(locator);
                                return found.isDisplayed() ? found : null;
                            } catch (Exception e) {
                                return null;
                            }
                        });
            } else {
                return WaitFactory.wait(driver, timeoutSeconds)
                        .until(WaitConditions.visible(locator));
            }
        } catch (Exception e) {
            throw new ElementNotFoundException(locator.toString(), e);
        }
    }

    protected List<WebElement> findChildren(By locator) {
        try {
            if (root != null) {
                return root.findElements(locator);
            }
            return driver.findElements(locator);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMMON ACTIONS
    // ══════════════════════════════════════════════════════════════════════════

    protected void type(By locator, String text) {
        findChild(locator).clear();
        findChild(locator).sendKeys(text);
        AutomationLogger.debug("Component typed '{}' into {}", text, locator);
    }

    protected void clickChild(By locator) {
        WebElement el;
        if (root != null) {
            el = WaitFactory.wait(driver)
                    .until(WaitConditions.clickable(root.findElement(locator)));
        } else {
            el = WaitFactory.wait(driver)
                    .until(WaitConditions.clickable(locator));
        }
        el.click();
    }

    protected String getTextChild(By locator) {
        return findChild(locator).getText().trim();
    }

    protected boolean isChildDisplayed(By locator) {
        try {
            return findChild(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForChildInvisibility(By locator) {
        if (root != null) {
            WaitFactory.wait(driver).until(d -> {
                try {
                    return !root.findElement(locator).isDisplayed();
                } catch (Exception e) {
                    return true;
                }
            });
        } else {
            WaitFactory.wait(driver).until(WaitConditions.invisible(locator));
        }
    }

    /** Returns the WebDriver for subclasses that need it. */
    protected WebDriver getDriver() {
        return driver;
    }

    /** Returns the root element. */
    protected WebElement root() {
        return root;
    }

    protected WebDriverWait waitFor() {
        return WaitFactory.wait(driver);
    }
}
