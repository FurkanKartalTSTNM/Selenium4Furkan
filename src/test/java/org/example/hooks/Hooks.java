package org.example.hooks;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.AfterStep;
import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.BeforeSuite;
import com.thoughtworks.gauge.ExecutionContext;
import org.example.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Hooks {

    @BeforeSuite
    public void beforeSuite() {
        // Suite başlamadan çalışır (global setup/logging vs.)
    }

    @AfterSuite
    public void afterSuite() {
        // Suite bittikten sonra çalışır
    }

    @BeforeScenario
    public void beforeScenario() {
        DriverManager.start();
    }

    @AfterScenario
    public void afterScenario() {
        DriverManager.stop();
    }

    @AfterStep
    public void takeScreenshot(ExecutionContext context) {
        try {
            WebDriver driver = DriverManager.get();
            if (driver == null) return;

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // reports/screenshots/<scenarioName>_timestamp.png
            String timestamp = new SimpleDateFormat("HH-mm-ss.SSS").format(new Date());
            String scenario = context.getCurrentScenario().getName().replaceAll("\\s+", "_");
            Path targetPath = Path.of("reports", "screenshots", scenario + "_" + timestamp + ".png");
            Files.createDirectories(targetPath.getParent());
            Files.copy(src.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("📸 Screenshot captured: " + targetPath);

        } catch (Exception e) {
            System.err.println("⚠️ Screenshot capture failed: " + e.getMessage());
        }
    }
}
