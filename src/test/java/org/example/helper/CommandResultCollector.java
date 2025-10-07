package org.example.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-safe collector for WebDriver command results and screenshots.
 * <p>
 * Use {@link #add(CommandResultLog)} to collect logs during test execution,
 * and {@link #flushToJson(String, String)} to persist all collected data into a JSON file.
 */
public final class CommandResultCollector {

    private static final List<CommandResultLog> LOGS =
            Collections.synchronizedList(new ArrayList<>());

    private CommandResultCollector() {
        // Utility class — prevent instantiation
    }

    /**
     * Adds a new command result entry.
     *
     * @param log command result to add
     */
    public static void add(CommandResultLog log) {
        if (log != null) {
            LOGS.add(log);
        }
    }

    /**
     * Writes all collected command results to a JSON file and clears the buffer.
     *
     * @param baseDir  directory to write into (created if missing)
     * @param fileName output JSON file name (e.g., "commands.json")
     */
    public static void flushToJson(String baseDir, String fileName) {
        if (LOGS.isEmpty()) {
            System.out.println("ℹ️ No command results to write.");
            return;
        }

        try {
            Path dir = Paths.get(baseDir);
            Files.createDirectories(dir);

            Path file = dir.resolve(fileName);
            ObjectMapper mapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(file.toFile(), new ArrayList<>(LOGS));
            System.out.printf("✅ Command results written (%d entries): %s%n",
                    LOGS.size(), file.toAbsolutePath());

            // Optional: clear after flush to avoid duplicate writes
            LOGS.clear();

        } catch (IOException e) {
            System.err.println("❌ Command results write error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("⚠️ Unexpected error while writing command results: " + e);
        }
    }
}
