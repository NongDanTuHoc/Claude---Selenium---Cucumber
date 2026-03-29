package com.company.automation.core.exception;

/**
 * Thrown when a UI element cannot be found within the configured timeout.
 */
public class ElementNotFoundException extends AutomationException {

    private final String locator;

    public ElementNotFoundException(String locator) {
        super("Element not found: " + locator);
        this.locator = locator;
    }

    public ElementNotFoundException(String locator, Throwable cause) {
        super("Element not found: " + locator, cause);
        this.locator = locator;
    }

    public String getLocator() {
        return locator;
    }
}
