package org.example.helper;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class ScreenshotUtil {
    public static String take(WebDriver d, String tag) {
        try {
            File src = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
            String fileName = Instant.now().toString().replace(":", "-") + "_" + tag + "_" + UUID.randomUUID() + ".png";
            Path out = Path.of("reports", "screenshots", fileName);
            Files.createDirectories(out.getParent());
            Files.copy(src.toPath(), out);
            return out.toString();
        } catch (Exception ignore) {
            return null;
        }
    }
}
