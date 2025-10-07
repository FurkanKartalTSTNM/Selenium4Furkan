package org.example.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandResultCollector {
    private static final List<CommandResultLog> LOGS =
            Collections.synchronizedList(new ArrayList<>());

    public static void add(CommandResultLog log) { LOGS.add(log); }

    public static void flushToJson(String baseDir, String fileName) {
        try {
            Path dir = Paths.get(baseDir);
            Files.createDirectories(dir);
            Path file = dir.resolve(fileName);
            ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            om.writeValue(file.toFile(), LOGS);
            System.out.println("✅ Command results written: " + file + " (count=" + LOGS.size() + ")");
        } catch (Exception e) {
            System.err.println("❌ Command results write error: " + e.getMessage());
        }
    }
}
