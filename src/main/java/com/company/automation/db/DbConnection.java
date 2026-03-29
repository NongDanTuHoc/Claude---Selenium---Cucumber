package com.company.automation.db;

import com.company.automation.core.logging.AutomationLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around a JDBC Connection with auto-cleanup semantics.
 * Use try-with-resources to guarantee connection closure.
 *
 * Usage:
 * <pre>
 * try (DbConnection db = new DbConnection(connection)) {
 *     List&lt;Map&lt;String, Object&gt;&gt; rows = db.select("SELECT * FROM users WHERE id = ?", 1);
 * }
 * </pre>
 */
public class DbConnection implements AutoCloseable {

    private final Connection connection;
    private boolean committed = false;

    public DbConnection(Connection connection) throws SQLException {
        this.connection = connection;
        this.connection.setAutoCommit(false);
    }

    // ── Transaction control ─────────────────────────────────────────────────

    public void commit() throws SQLException {
        connection.commit();
        committed = true;
        AutomationLogger.debug("DB transaction committed");
    }

    public void rollback() {
        try {
            if (!committed) {
                connection.rollback();
                AutomationLogger.debug("DB transaction rolled back");
            }
        } catch (SQLException e) {
            AutomationLogger.warn("Rollback failed: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (!committed) {
                connection.rollback(); // auto-rollback uncommitted transactions
            }
            connection.close();
            AutomationLogger.debug("DB connection closed");
        } catch (SQLException e) {
            AutomationLogger.warn("Failed to close DB connection: {}", e.getMessage());
        }
    }

    // ── Select — returns List of Map<String, Object> ────────────────────────

    /**
     * Executes a SELECT and returns all rows as a list of column→value maps.
     * Use ? placeholders with varargs.
     */
    public List<Map<String, Object>> select(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement ps = prepare(sql, params);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Executes a SELECT and returns the first row only, or null.
     */
    public Map<String, Object> selectOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = select(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ── Update / Insert / Delete ─────────────────────────────────────────────

    /**
     * Executes an INSERT, UPDATE, or DELETE. Returns affected row count.
     */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(sql, params)) {
            int count = ps.executeUpdate();
            AutomationLogger.debug("DB update: {} rows affected — {}", count, sql);
            return count;
        }
    }

    /**
     * Executes INSERT and returns the generated key (first auto-increment column).
     */
    public long insertAndGetKey(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(sql, params)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("No generated key returned");
            }
        }
    }

    // ── DDL ─────────────────────────────────────────────────────────────────

    /** Executes a DDL statement (CREATE, DROP, ALTER, etc.). */
    public void execute(String sql) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
            AutomationLogger.debug("DB DDL executed: {}", sql);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    /** Returns the raw JDBC connection for advanced use. */
    public Connection unwrap() {
        return connection;
    }
}
