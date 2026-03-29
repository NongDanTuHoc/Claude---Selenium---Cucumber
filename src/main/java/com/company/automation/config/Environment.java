package com.company.automation.config;

/**
 * Supported execution environments.
 */
public enum Environment {
    DEV("dev"),
    STAGING("staging"),
    PROD("prod");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Environment fromString(String env) {
        if (env == null || env.isBlank()) {
            return DEV;
        }
        String normalized = env.trim().toLowerCase();
        for (Environment e : values()) {
            if (e.value.equals(normalized)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                "Unknown environment: [" + env + "]. Supported: dev, staging, prod");
    }
}
