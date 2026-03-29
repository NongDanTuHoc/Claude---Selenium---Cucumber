package com.company.automation.core.driver;

/**
 * Supported browser types for local and remote execution.
 */
public enum DriverType {
    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("edge"),
    REMOTE("remote");

    private final String value;

    DriverType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DriverType fromString(String name) {
        if (name == null || name.isBlank()) {
            return CHROME;
        }
        for (DriverType dt : values()) {
            if (dt.value.equalsIgnoreCase(name.trim())) {
                return dt;
            }
        }
        throw new IllegalArgumentException(
                "Unknown driver type: [" + name + "]. Supported: chrome, firefox, edge");
    }
}
