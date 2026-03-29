package com.company.automation.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jackson-based JSON utilities for test data parsing and serialisation.
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {}

    // ── ObjectMapper access ──────────────────────────────────────────────────

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    // ── Parse from various sources ───────────────────────────────────────────

    public static <T> T fromFile(File file, Class<T> type) {
        try {
            return MAPPER.readValue(file, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error (file: " + file + ")", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file: " + file, e);
        }
    }

    public static <T> T fromResource(String resourcePath, Class<T> type) {
        try {
            return MAPPER.readValue(getResourceUrl(resourcePath), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error (resource: " + resourcePath + ")", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON resource: " + resourcePath, e);
        }
    }

    public static <T> T fromString(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error", e);
        }
    }

    public static <T> T fromInputStream(InputStream is, Class<T> type) {
        try {
            return MAPPER.readValue(is, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error from stream", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON from stream", e);
        }
    }

    // ── Parse collections ─────────────────────────────────────────────────────

    /**
     * Parses a JSON array resource into a List of the given type.
     * Example: fromResourceList("testdata/users.json", User.class)
     */
    public static <T> List<T> fromResourceList(String resourcePath, Class<T> elementType) {
        try {
            JavaType listType = MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return MAPPER.readValue(getResourceUrl(resourcePath), listType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error (resource list: " + resourcePath + ")", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON resource list: " + resourcePath, e);
        }
    }

    /**
     * Parses a JSON object resource into a Map&lt;String, Object&gt;.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromResourceMap(String resourcePath) {
        try {
            return MAPPER.readValue(getResourceUrl(resourcePath), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error (resource map: " + resourcePath + ")", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON resource map: " + resourcePath, e);
        }
    }

    // ── Navigate JSON tree ───────────────────────────────────────────────────

    /**
     * Returns a JsonNode (tree) for navigating nested JSON without POJO.
     */
    public static JsonNode treeFromResource(String resourcePath) {
        try {
            return MAPPER.readTree(getResourceUrl(resourcePath));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON tree: " + resourcePath, e);
        }
    }

    /**
     * Returns an ArrayNode for the given JSON array field.
     */
    public static ArrayNode arrayField(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || !node.isArray()) {
            return MAPPER.createArrayNode();
        }
        return (ArrayNode) node;
    }

    // ── Serialise ────────────────────────────────────────────────────────────

    /**
     * Serialises an object to a pretty-printed JSON String.
     */
    public static String toPrettyString(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialisation error", e);
        }
    }

    /**
     * Serialises an object to a compact JSON String.
     */
    public static String toString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialisation error", e);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static URL getResourceUrl(String resourcePath) {
        URL url = JsonUtil.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("JSON resource not found on classpath: " + resourcePath);
        }
        return url;
    }
}
