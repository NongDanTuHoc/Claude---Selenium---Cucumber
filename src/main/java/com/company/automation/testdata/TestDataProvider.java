package com.company.automation.testdata;

import com.company.automation.core.logging.AutomationLogger;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TestNG DataProvider factory — returns typed test data for Cucumber step-defs and test methods.
 *
 * Usage in a test class:
 * <pre>
 * &#64;Test(dataProvider = "json", dataProviderClass = TestDataProvider.class)
 * public void testLogin(UserTestData user) { ... }
 *
 * // Corresponding DataProvider:
 * &#64;DataProvider(name = "json")
 * public static Iterator&lt;Object[]&gt; json(Method m) {
 *     return TestDataProvider.jsonProvider("login/valid_users.json", UserTestData.class);
 * }
 * </pre>
 */
public final class TestDataProvider {

    private TestDataProvider() {}

    // ── DataProvider builders ─────────────────────────────────────────────────

    /**
     * Returns an Iterator of Object[] from a JSON array resource.
     * Each array element is deserialised to the given type.
     */
    public static <T> Iterator<Object[]> jsonProvider(String resourcePath, Class<T> type) {
        List<T> items = TestDataLoader.loadArrayAs(resourcePath, type);
        AutomationLogger.debug("DataProvider '{}' loaded {} records from {}",
                resourcePath, items.size(), resourcePath);
        return toObjectArrayIterator(items);
    }

    /**
     * Returns an Iterator of Object[] from a JSON array resource, filtered by a predicate.
     *
     * @param resourcePath JSON file under testdata/
     * @param type        POJO class
     * @param filter      JsonNode → boolean; return true to include the record
     */
    public static <T> Iterator<Object[]> jsonProviderFiltered(
            String resourcePath, Class<T> type, java.util.function.Function<JsonNode, Boolean> filter) {

        List<JsonNode> all = TestDataLoader.loadArray(resourcePath);
        List<T> filtered = all.stream()
                .filter(n -> Boolean.TRUE.equals(filter.apply(n)))
                .map(n -> TestDataLoader.loadAs(resourcePath, type))
                .toList();

        AutomationLogger.debug("DataProvider '{}' loaded {} of {} records (filtered) from {}",
                resourcePath, filtered.size(), all.size(), resourcePath);
        return toObjectArrayIterator(filtered);
    }

    /**
     * Returns an Iterator of Object[] from a CSV resource (header-keyed maps).
     * Each row is wrapped as a Map&lt;String, String&gt;.
     */
    public static Iterator<Object[]> csvProvider(String resourcePath) {
        List<Map<String, String>> rows = com.company.automation.utilities.DataTableUtil
                .parseCsvWithHeaders(java.nio.file.Paths.get(resourcePath));
        AutomationLogger.debug("DataProvider '{}' loaded {} rows", resourcePath, rows.size());
        return toObjectArrayIterator(rows);
    }

    // ── Converters ───────────────────────────────────────────────────────────

    /**
     * Wraps a list of T into Iterator&lt;Object[]&gt; (required by TestNG DataProvider).
     */
    public static <T> Iterator<Object[]> toObjectArrayIterator(List<T> items) {
        return items.stream()
                .map(item -> new Object[]{item})
                .iterator();
    }

    /**
     * Wraps a single object as a single-entry Object[].
     */
    public static Object[] toObjectArray(Object item) {
        return new Object[]{item};
    }
}
