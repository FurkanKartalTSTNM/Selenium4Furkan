package org.example.helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

public class CommandJsonSink {
    private final Path file;
    public CommandJsonSink(String path) {
        this.file = Paths.get(path);
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) Files.createFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Log file init failed: " + path, e);
        }
    }

    // Basit JSONL (her satır bir JSON), senkronize.
    public synchronized void write(String name, String sessionId, Long status,
                                   long ms, Map<String, ?> params,
                                   String httpMethod, String uri,
                                   String screenshotPath) {
        String ts = Instant.now().toString();
        String p = params != null ? escape(params.toString()) : null;
        String line = String.format(
                "{\"ts\":\"%s\",\"name\":\"%s\",\"sid\":\"%s\",\"status\":%s,\"ms\":%d,\"method\":%s,\"uri\":%s,\"params\":%s,\"screenshot\":%s}\n",
                ts, escape(name), escape(sessionId),
                status == null ? "null" : status.toString(),
                ms,
                httpMethod == null ? "null" : "\"" + escape(httpMethod) + "\"",
                uri == null ? "null" : "\"" + escape(uri) + "\"",
                p == null ? "null" : "\"" + p + "\"",
                screenshotPath == null ? "null" : "\"" + escape(screenshotPath) + "\""
        );
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.toFile(), true))) {
            bw.write(line);
        } catch (IOException e) {
            // yut: log yazılamadıysa testi bozma
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
