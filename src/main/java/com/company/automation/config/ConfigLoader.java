package com.company.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads environment-specific property files.
 * Resolution order:
 *   1. -Denv system property (highest priority)
 *   2. ENV environment variable
 *   3. Falls back to DEV
 *
 * File must exist at: config/env/config.[env].properties
 */
public class ConfigLoader {

    private static final Logger LOG = LogManager.getLogger(ConfigLoader.class);
    private static final String CONFIG_DIR = "config/env";
    private static final String CONFIG_FILE_PREFIX = "config.";
    private static final String CONFIG_FILE_SUFFIX = ".properties";

    private static final Properties properties = new Properties();

    static {
        load();
    }

    private ConfigLoader() {}

    /**
     * Reads -Denv, then ENV env-var, then defaults to DEV.
     */
    private static void load() {
        String env = System.getProperty("env",
                System.getenv().getOrDefault("ENV", Environment.DEV.getValue()));
        Environment target = Environment.fromString(env);

        String fileName = CONFIG_FILE_PREFIX + target.getValue() + CONFIG_FILE_SUFFIX;
        Path configPath = Paths.get(CONFIG_DIR, fileName);

        if (Files.exists(configPath)) {
            try (InputStream is = Files.newInputStream(configPath)) {
                properties.load(is);
                LOG.info("Loaded config from: {}", configPath.toAbsolutePath());
            } catch (IOException e) {
                LOG.error("Failed to load config file: {}", configPath, e);
                throw new RuntimeException("Config load failed", e);
            }
        } else {
            // Fallback: load from classpath
            String resource = "/" + CONFIG_DIR + "/" + fileName;
            try (InputStream is = ConfigLoader.class.getResourceAsStream(resource)) {
                if (is != null) {
                    properties.load(is);
                    LOG.info("Loaded config from classpath: {}", resource);
                } else {
                    LOG.warn("Config file not found: {}. Using empty defaults.", configPath);
                }
            } catch (IOException e) {
                LOG.error("Failed to load config from classpath: {}", resource, e);
            }
        }

        // Always stamp the resolved env
        properties.setProperty(ConfigKeys.ENV, target.getValue());
        LOG.info("Active environment: {}", target.getValue().toUpperCase());
    }

    // ── Property accessors ───────────────────────────────────────────────────

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer for key [{}]: '{}'. Using default: {}", key, val, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBool(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public static Environment getEnvironment() {
        return Environment.fromString(get(ConfigKeys.ENV, Environment.DEV.getValue()));
    }

    /** Reloads config from disk (useful in long-running processes). */
    public static void reload() {
        properties.clear();
        load();
    }
}
