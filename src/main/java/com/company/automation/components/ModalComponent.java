package com.company.automation.components;

import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Reusable modal / dialog fragment helper.
 * Handles open, close, wait-for-open, wait-for-close, and backdrop interactions.
 */
public class ModalComponent extends BaseComponent {

    private final By modalLocator;
    private final By closeButtonLocator;
    private final By backdropLocator;

    public ModalComponent(WebDriver driver, By modalLocator) {
        super(driver);
        this.modalLocator = modalLocator;
        this.closeButtonLocator = By.cssSelector("[data-dismiss='modal'], .btn-close, button.close");
        this.backdropLocator = By.cssSelector(".modal-backdrop, .modal-open");
    }

    public ModalComponent(WebDriver driver, By modalLocator, By closeButtonLocator) {
        super(driver);
        this.modalLocator = modalLocator;
        this.closeButtonLocator = closeButtonLocator;
        this.backdropLocator = By.cssSelector(".modal-backdrop");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WAIT HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Waits until the modal is visible. */
    public void waitForOpen() {
        WaitFactory.wait(driver).until(WaitConditions.visible(modalLocator));
        AutomationLogger.debug("Modal opened: {}", modalLocator);
    }

    /** Waits until the modal is gone from DOM. */
    public void waitForClose() {
        WaitFactory.wait(driver, 10).until(WaitConditions.invisible(modalLocator));
        AutomationLogger.debug("Modal closed: {}", modalLocator);
    }

    /** Returns true if the modal is currently visible. */
    public boolean isOpen() {
        try {
            WebElement modal = driver.findElement(modalLocator);
            return modal.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OPEN / CLOSE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Subclasses call this after triggering the action that opens the modal.
     * Usage: new SomeModal(driver).waitForOpen();
     */
    public void assertOpen() {
        if (!isOpen()) {
            throw new AssertionError("Expected modal to be open: " + modalLocator);
        }
    }

    /** Closes the modal by clicking the close button. */
    public void close() {
        try {
            WebDriverWait w = WaitFactory.wait(driver, 5);
            WebElement closeBtn = w.until(
                    WaitConditions.clickable(
                            driver.findElement(modalLocator).findElement(closeButtonLocator)));
            closeBtn.click();
            AutomationLogger.debug("Modal closed via button: {}", closeButtonLocator);
        } catch (Exception e) {
            // Try closing via ESC key
            try {
                driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
            } catch (Exception ignored) {}
        }
        waitForClose();
    }

    /** Closes via backdrop click. */
    public void closeViaBackdrop() {
        try {
            WebElement backdrop = driver.findElement(backdropLocator);
            backdrop.click();
            waitForClose();
        } catch (Exception e) {
            AutomationLogger.warn("Could not close modal via backdrop: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONTENT HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns the modal's title text. */
    public String getTitle() {
        By titleLocator = By.cssSelector(".modal-title, [role='dialog'] h4, h2.modal-title");
        try {
            WebElement modal = driver.findElement(modalLocator);
            return modal.findElement(titleLocator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Returns the modal body text. */
    public String getBodyText() {
        By bodyLocator = By.cssSelector(".modal-body");
        try {
            WebElement modal = driver.findElement(modalLocator);
            return modal.findElement(bodyLocator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Clicks a button inside the modal by its text. */
    public void clickButton(String buttonText) {
        By btnLocator = By.xpath(
                ".//button[normalize-space()='" + buttonText + "'] | "
              + ".//a[normalize-space()='" + buttonText + "']");
        WebElement modal = driver.findElement(modalLocator);
        WebElement btn = WaitFactory.wait(driver, 5)
                .until(WaitConditions.clickable(modal.findElement(btnLocator)));
        btn.click();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ANIMATION / TIMING
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Waits for a CSS transition to complete before proceeding.
     * Some modals animate open — call this before interacting.
     */
    public void waitForTransition() {
        try {
            Thread.sleep(300); // one-shot pause for CSS transition, not a poll
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
