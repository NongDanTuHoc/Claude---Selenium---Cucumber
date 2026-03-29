package com.company.automation.utilities;

import com.company.automation.core.logging.AutomationLogger;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * File and resource utilities.
 */
public final class FileUtil {

    private FileUtil() {}

    // ── Resource path resolution ─────────────────────────────────────────────

    /**
     * Returns the absolute path of a resource on the classpath.
     * Works in both IDE and Maven test-classes output dirs.
     */
    public static Path getResourcePath(String resourceName) {
        URL url = FileUtil.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found on classpath: " + resourceName);
        }
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid resource URI: " + url, e);
        }
    }

    /**
     * Opens an InputStream for a classpath resource.
     * Caller is responsible for closing the stream.
     */
    public static InputStream getResourceStream(String resourceName) {
        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        return is;
    }

    // ── Read files ────────────────────────────────────────────────────────────

    /**
     * Reads a classpath resource as a String (UTF-8).
     */
    public static String readResource(String resourceName) {
        try (InputStream is = getResourceStream(resourceName)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourceName, e);
        }
    }

    /**
     * Reads a local filesystem file as a String.
     */
    public static String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    /**
     * Reads a file from a relative path (from project root).
     */
    public static String readFile(String relativePath) {
        return readFile(Paths.get(relativePath));
    }

    // ── Write files ─────────────────────────────────────────────────────────

    /**
     * Writes content to a file, creating parent dirs as needed.
     */
    public static void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            AutomationLogger.info("File written: {}", path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    // ── Copy ─────────────────────────────────────────────────────────────────

    /**
     * Copies a resource from classpath to a destination file.
     */
    public static void copyResourceToFile(String resourceName, Path dest) {
        try (InputStream is = getResourceStream(resourceName)) {
            Files.createDirectories(dest.getParent());
            Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
            AutomationLogger.info("Copied resource {} -> {}", resourceName, dest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy resource: " + resourceName, e);
        }
    }

    // ── Directory helpers ─────────────────────────────────────────────────────

    /**
     * Creates a directory (and parents) if it does not exist.
     */
    public static void ensureDir(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                AutomationLogger.info("Created directory: {}", dir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + dir, e);
            }
        }
    }

    /**
     * Deletes a file or directory recursively.
     */
    public static void delete(Path path) {
        if (!Files.exists(path)) return;
        try {
            if (Files.isDirectory(path)) {
                org.apache.commons.io.FileUtils.deleteDirectory(path.toFile());
            } else {
                Files.delete(path);
            }
            AutomationLogger.info("Deleted: {}", path);
        } catch (IOException e) {
            AutomationLogger.warn("Failed to delete: {} — {}", path, e.getMessage());
        }
    }
}
