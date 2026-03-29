package com.company.automation.testdata;

import com.company.automation.core.logging.AutomationLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.company.automation.utilities.JsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Loads JSON test data files from src/test/resources/testdata/.
 * All resource paths are resolved relative to the test-resources classpath root.
 */
public final class TestDataLoader {

    /** Base path for all test data files under test/resources. */
    private static final String BASE_PATH = "testdata/";

    private TestDataLoader() {}

    // ── Load helpers ──────────────────────────────────────────────────────────

    /**
     * Loads a JSON file and returns the root JsonNode for navigation.
     *
     * @param relativePath path relative to testdata/, e.g. "login/valid_users.json"
     */
    public static JsonNode load(String relativePath) {
        String resourcePath = BASE_PATH + stripLeadingSlash(relativePath);
        try {
            JsonNode node = JsonUtil.treeFromResource(resourcePath);
            AutomationLogger.debug("Loaded test data: {}", resourcePath);
            return node;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data: " + resourcePath, e);
        }
    }

    /**
     * Loads a JSON array resource and returns it as a List of JsonNodes.
     */
    public static List<JsonNode> loadArray(String relativePath) {
        JsonNode root = load(relativePath);
        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected JSON array in: " + relativePath);
        }
        ArrayNode arr = (ArrayNode) root;
        List<JsonNode> list = new ArrayList<>();
        arr.forEach(list::add);
        return list;
    }

    /**
     * Loads a JSON array resource and maps each element through a JsonNode→T transformer.
     *
     * @param relativePath path relative to testdata/
     * @param type        the POJO class to deserialise each array element to
     * @param <T>         POJO type
     */
    public static <T> List<T> loadArrayAs(String relativePath, Class<T> type) {
        String resourcePath = BASE_PATH + stripLeadingSlash(relativePath);
        try {
            return JsonUtil.fromResourceList(resourcePath, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data array as " + type.getName() + ": " + resourcePath, e);
        }
    }

    /**
     * Loads a JSON object resource as a typed POJO.
     */
    public static <T> T loadAs(String relativePath, Class<T> type) {
        String resourcePath = BASE_PATH + stripLeadingSlash(relativePath);
        try {
            return JsonUtil.fromResource(resourcePath, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data as " + type.getName() + ": " + resourcePath, e);
        }
    }

    // ── Navigate loaded JSON ─────────────────────────────────────────────────

    /**
     * Returns the text value of a field, or empty string if absent.
     */
    public static String field(JsonNode node, String fieldName) {
        JsonNode n = node.get(fieldName);
        return (n == null || n.isNull()) ? "" : n.asText();
    }

    /**
     * Returns the int value of a field, or the default if absent/non-numeric.
     */
    public static int intField(JsonNode node, String fieldName, int defaultValue) {
        JsonNode n = node.get(fieldName);
        return (n == null || n.isNull()) ? defaultValue : n.asInt(defaultValue);
    }

    /**
     * Returns the boolean value of a field, or false if absent.
     */
    public static boolean boolField(JsonNode node, String fieldName) {
        JsonNode n = node.get(fieldName);
        return n != null && !n.isNull() && n.asBoolean(false);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
