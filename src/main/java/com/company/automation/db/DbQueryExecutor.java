package com.company.automation.db;

import com.company.automation.core.logging.AutomationLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Convenient facade for common DB operations using the active DbConnection.
 * This class manages the connection lifecycle — callers don't open/close directly.
 *
 * Usage:
 * <pre>
 * List&lt;Map&lt;String, Object&gt;&gt; users = DbQueryExecutor.select("SELECT * FROM users WHERE role = ?", "admin");
 * DbQueryExecutor.insert("INSERT INTO orders (user_id, total) VALUES (?, ?)", 1, 99.99);
 * </pre>
 */
public final class DbQueryExecutor {

    private DbQueryExecutor() {}

    // ── SELECT ─────────────────────────────────────────────────────────────

    /**
     * Executes a SELECT and returns all rows.
     * Use ? placeholders.
     */
    public static List<Map<String, Object>> select(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            List<Map<String, Object>> rows = db.select(sql, params);
            AutomationLogger.debug("SELECT returned {} rows — {}", rows.size(), sql);
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("DB SELECT failed: " + sql, e);
        }
    }

    /**
     * Executes a SELECT and returns the first row, or null.
     */
    public static Map<String, Object> selectOne(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            return db.selectOne(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("DB SELECT failed: " + sql, e);
        }
    }

    /**
     * Executes a SELECT and returns a scalar value (first cell of first row).
     */
    @SuppressWarnings("unchecked")
    public static <T> T scalar(String sql, Object... params) {
        Map<String, Object> row = selectOne(sql, params);
        if (row == null || row.isEmpty()) {
            return null;
        }
        return (T) row.values().iterator().next();
    }

    // ── INSERT ─────────────────────────────────────────────────────────────

    /**
     * Executes an INSERT. Returns affected row count.
     */
    public static int insert(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            int count = db.update(sql, params);
            db.commit();
            AutomationLogger.info("INSERT: {} rows — {}", count, sql);
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("DB INSERT failed: " + sql, e);
        }
    }

    /**
     * Executes an INSERT and returns the auto-generated key.
     */
    public static long insertAndGetKey(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            long key = db.insertAndGetKey(sql, params);
            db.commit();
            AutomationLogger.info("INSERT key={} — {}", key, sql);
            return key;
        } catch (SQLException e) {
            throw new RuntimeException("DB INSERT failed: " + sql, e);
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    /**
     * Executes an UPDATE. Returns affected row count.
     */
    public static int update(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            int count = db.update(sql, params);
            db.commit();
            AutomationLogger.info("UPDATE: {} rows — {}", count, sql);
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("DB UPDATE failed: " + sql, e);
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /**
     * Executes a DELETE. Returns affected row count.
     */
    public static int delete(String sql, Object... params) {
        try (DbConnection db = DbConnectionFactory.open()) {
            int count = db.update(sql, params);
            db.commit();
            AutomationLogger.info("DELETE: {} rows — {}", count, sql);
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("DB DELETE failed: " + sql, e);
        }
    }

    // ── DDL ────────────────────────────────────────────────────────────────

    /** Executes DDL (CREATE, DROP, ALTER). */
    public static void execute(String sql) {
        try (DbConnection db = DbConnectionFactory.open()) {
            db.execute(sql);
            db.commit();
        } catch (SQLException e) {
            throw new RuntimeException("DB DDL failed: " + sql, e);
        }
    }
}
