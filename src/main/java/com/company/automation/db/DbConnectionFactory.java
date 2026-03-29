package com.company.automation.db;

import com.company.automation.config.ConfigKeys;
import com.company.automation.config.ConfigLoader;
import com.company.automation.core.logging.AutomationLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory for creating DbConnection instances from ConfigLoader settings.
 * Supports MySQL and PostgreSQL.
 */
public final class DbConnectionFactory {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    static {
        try {
            Class.forName(MYSQL_DRIVER);
            Class.forName(POSTGRES_DRIVER);
            AutomationLogger.info("DB drivers registered: MySQL, PostgreSQL");
        } catch (ClassNotFoundException e) {
            AutomationLogger.warn("DB driver class not found on classpath — DB features disabled");
        }
    }

    private DbConnectionFactory() {}

    // ── Entry point ─────────────────────────────────────────────────────────

    /**
     * Creates a DbConnection using settings from ConfigLoader.
     * Set db.enabled=false in the config to disable DB entirely.
     */
    public static DbConnection open() {
        if (!ConfigLoader.getBool(ConfigKeys.DB_ENABLED, false)) {
            throw new IllegalStateException("Database is disabled in config (db.enabled=false)");
        }
        String dbType   = ConfigLoader.get(ConfigKeys.DB_TYPE, "mysql");
        String host     = ConfigLoader.get(ConfigKeys.DB_HOST, "localhost");
        int    port     = ConfigLoader.getInt(ConfigKeys.DB_PORT, 3306);
        String dbName   = ConfigLoader.get(ConfigKeys.DB_NAME, "");
        String user     = ConfigLoader.get(ConfigKeys.DB_USERNAME, "root");
        String password = ConfigLoader.get(ConfigKeys.DB_PASSWORD, "");

        String url = buildUrl(dbType, host, port, dbName);
        return open(url, user, password);
    }

    /**
     * Opens a DbConnection with an explicit JDBC URL.
     */
    public static DbConnection open(String jdbcUrl, String username, String password) {
        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            AutomationLogger.info("DB connection opened: {}", jdbcUrl);
            return new DbConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open DB connection: " + jdbcUrl, e);
        }
    }

    // ── URL builders ────────────────────────────────────────────────────────

    private static String buildUrl(String dbType, String host, int port, String dbName) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return String.format(
                        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                        host, port, dbName);
            case "postgresql":
            case "postgres":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            default:
                throw new IllegalArgumentException("Unsupported DB type: " + dbType
                        + ". Supported: mysql, postgresql");
        }
    }

    /** Returns the configured DB type string (mysql | postgresql). */
    public static String dbType() {
        return ConfigLoader.get(ConfigKeys.DB_TYPE, "mysql");
    }

    /** Returns true if DB is enabled in the active config. */
    public static boolean isEnabled() {
        return ConfigLoader.getBool(ConfigKeys.DB_ENABLED, false);
    }
}
