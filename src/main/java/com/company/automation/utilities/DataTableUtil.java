package com.company.automation.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for parsing tabular data (CSV, TSV, pipe-delimited).
 * Supports both classpath resources and filesystem files.
 */
public final class DataTableUtil {

    private static final String DEFAULT_DELIMITER = ",";

    private DataTableUtil() {}

    // ── Parse CSV / delimited ─────────────────────────────────────────────────

    /**
     * Parses a delimited file into a list of string arrays (rows).
     *
     * @param filePath   absolute or classpath-relative path
     * @param delimiter  field separator (e.g. ",", "\\t", "\\|")
     * @param skipHeader true to skip the first row
     */
    public static List<String[]> parse(Path filePath, String delimiter, boolean skipHeader) {
        List<String> lines;
        try {
            if (Files.exists(filePath)) {
                lines = Files.readAllLines(filePath);
            } else {
                // Try as classpath resource
                String content = FileUtil.readResource(filePath.toString());
                lines = Arrays.asList(content.split("\\r?\\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data file: " + filePath, e);
        }

        List<String[]> rows = new ArrayList<>();
        for (int i = skipHeader ? 1 : 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            rows.add(line.split(delimiter, -1));
        }
        return rows;
    }

    /**
     * Parses a CSV file (comma-delimited), skipping the header row.
     */
    public static List<String[]> parseCsv(Path filePath) {
        return parse(filePath, DEFAULT_DELIMITER, true);
    }

    /**
     * Parses a classpath resource as CSV.
     */
    public static List<String[]> parseCsvResource(String resourcePath) {
        return parse(Path.of(resourcePath), DEFAULT_DELIMITER, true);
    }

    // ── Parse with headers ────────────────────────────────────────────────────

    /**
     * Parses a delimited file into a list of maps, using the header row as keys.
     *
     * @return List of Map&lt;columnName, cellValue&gt;
     */
    public static List<Map<String, String>> parseWithHeaders(Path filePath, String delimiter) {
        List<String[]> rows = parse(filePath, delimiter, false);
        if (rows.isEmpty()) return List.of();

        String[] headers = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] cells = rows.get(i);
            Map<String, String> row = new HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                String value = j < cells.length ? cells[j].trim() : "";
                row.put(headers[j].trim(), value);
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Parses a CSV as a list of header-keyed maps.
     */
    public static List<Map<String, String>> parseCsvWithHeaders(Path filePath) {
        return parseWithHeaders(filePath, DEFAULT_DELIMITER);
    }

    // ── Flatten / transform ───────────────────────────────────────────────────

    /**
     * Extracts a single column as a List of Strings.
     *
     * @param rows    parsed rows (from parseCsvWithHeaders)
     * @param column  header name
     */
    public static List<String> column(List<Map<String, String>> rows, String column) {
        List<String> values = new ArrayList<>();
        for (Map<String, String> row : rows) {
            values.add(row.getOrDefault(column, ""));
        }
        return values;
    }

    /**
     * Transposes a list of string arrays (rows → columns) for column-based iteration.
     */
    public static List<String[]> transpose(List<String[]> rows) {
        if (rows.isEmpty()) return List.of();
        int cols = rows.get(0).length;
        List<String[]> result = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            String[] col = new String[rows.size()];
            for (int r = 0; r < rows.size(); r++) {
                col[r] = r < rows.get(r).length ? rows.get(r)[r] : "";
            }
            result.add(col);
        }
        return result;
    }
}
