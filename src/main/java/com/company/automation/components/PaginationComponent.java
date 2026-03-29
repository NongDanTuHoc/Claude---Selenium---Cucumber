package com.company.automation.components;

import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Reusable pagination fragment helper.
 * Supports standard first/prev/next/last/page-number navigation patterns.
 */
public class PaginationComponent extends BaseComponent {

    // Default selectors — subclasses can override via setters
    private By firstBtn       = By.cssSelector("a[aria-label='First'], a.page-link[title='First']");
    private By prevBtn        = By.cssSelector("a[aria-label='Previous'], a.page-link[title='Previous'], li.page-item.previous a");
    private By nextBtn        = By.cssSelector("a[aria-label='Next'], a.page-link[title='Next'], li.page-item.next a");
    private By lastBtn        = By.cssSelector("a[aria-label='Last'], a.page-link[title='Last']");
    private By pageNumbers    = By.cssSelector("li.page-item:not(.active):not(.disabled) a.page-link");
    private By activePage     = By.cssSelector("li.page-item.active a.page-link");
    private By disabledPrev  = By.cssSelector("li.page-item.previous.disabled, li.page-item.previous[class*='disabled']");
    private By disabledNext  = By.cssSelector("li.page-item.next.disabled, li.page-item.next[class*='disabled']");
    private By pageInfoText  = By.cssSelector(".pagination-info, .page-info, [class*='pagination-info']");

    /** Root locator for the pagination nav element. */
    private final By paginationRoot;

    public PaginationComponent(WebDriver driver, By paginationRoot) {
        super(driver);
        this.paginationRoot = paginationRoot;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    /** Navigates to the next page. */
    public void clickNext() {
        WebElement btn = getNavButton(nextBtn, disabledNext);
        btn.click();
        AutomationLogger.debug("Pagination: clicked Next");
        WaitFactory.waitForPageLoad(driver);
    }

    /** Navigates to the previous page. */
    public void clickPrevious() {
        WebElement btn = getNavButton(prevBtn, disabledPrev);
        btn.click();
        AutomationLogger.debug("Pagination: clicked Previous");
        WaitFactory.waitForPageLoad(driver);
    }

    /** Navigates to the first page. */
    public void clickFirst() {
        WebElement btn = findNavButton(firstBtn);
        btn.click();
        AutomationLogger.debug("Pagination: clicked First");
        WaitFactory.waitForPageLoad(driver);
    }

    /** Navigates to the last page. */
    public void clickLast() {
        WebElement btn = findNavButton(lastBtn);
        btn.click();
        AutomationLogger.debug("Pagination: clicked Last");
        WaitFactory.waitForPageLoad(driver);
    }

    /**
     * Navigates to a specific page number.
     * Page numbers start at 1.
     */
    public void goToPage(int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1, got: " + pageNumber);
        }
        List<WebElement> pages = findPageNumbers();
        if (pageNumber > pages.size()) {
            throw new IllegalArgumentException(
                    "Page " + pageNumber + " is out of range. Available: " + pages.size());
        }
        pages.get(pageNumber - 1).click();
        AutomationLogger.debug("Pagination: went to page {}", pageNumber);
        WaitFactory.waitForPageLoad(driver);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STATE
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns the currently active page number (1-based). */
    public int getCurrentPage() {
        try {
            WebElement root = driver.findElement(paginationRoot);
            WebElement active = root.findElement(activePage);
            return Integer.parseInt(active.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    /** Returns true if there is a next page. */
    public boolean hasNext() {
        try {
            WebElement root = driver.findElement(paginationRoot);
            root.findElement(disabledNext);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** Returns true if there is a previous page. */
    public boolean hasPrevious() {
        try {
            WebElement root = driver.findElement(paginationRoot);
            root.findElement(disabledPrev);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** Returns true if pagination is present on the page. */
    public boolean isPresent() {
        try {
            WebElement root = driver.findElement(paginationRoot);
            return root.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TEXT INFO
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns pagination info text (e.g. "Showing 1-20 of 120"). */
    public String getInfoText() {
        try {
            WebElement root = driver.findElement(paginationRoot);
            return root.findElement(pageInfoText).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Returns total number of clickable page buttons (excluding active/disabled). */
    public int getPageCount() {
        return findPageNumbers().size();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private List<WebElement> findPageNumbers() {
        WebElement root = driver.findElement(paginationRoot);
        return WaitFactory.wait(driver).until(d -> {
            List<WebElement> els = root.findElements(pageNumbers);
            return els.isEmpty() ? null : els;
        });
    }

    private WebElement findNavButton(By locator) {
        WebElement root = driver.findElement(paginationRoot);
        return WaitFactory.wait(driver, 5)
                .until(WaitConditions.clickable(root.findElement(locator)));
    }

    /**
     * Finds a nav button only if it is NOT disabled.
     */
    private WebElement getNavButton(By buttonLocator, By disabledLocator) {
        WebElement rootEl = driver.findElement(paginationRoot);
        List<WebElement> disabled = rootEl.findElements(disabledLocator);
        if (!disabled.isEmpty()) {
            throw new IllegalStateException("Navigation button is disabled: " + buttonLocator);
        }
        return WaitFactory.wait(driver, 5)
                .until(WaitConditions.clickable(rootEl.findElement(buttonLocator)));
    }
}
