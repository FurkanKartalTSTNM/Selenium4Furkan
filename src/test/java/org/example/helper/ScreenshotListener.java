package org.example.helper;


import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class ScreenshotListener implements WebDriverListener {

    private void capture(WebDriver d, String tag) {
        try {
            File src = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
            Path out = Path.of("reports", "screenshots",
                    Instant.now().toString().replace(":", "-") + "_" + tag + "_" + UUID.randomUUID() + ".png");
            Files.createDirectories(out.getParent());
            Files.copy(src.toPath(), out);
            System.out.println("📸 Screenshot saved: " + out);
        } catch (Exception ignored) {}
    }

    @Override
    public void onError(Object target, java.lang.reflect.Method method, Object[] args, InvocationTargetException e) {
        if (target instanceof WebDriver) {
            WebDriver d = (WebDriver) target;
            capture(d, method.getName());
        }
    }
}
