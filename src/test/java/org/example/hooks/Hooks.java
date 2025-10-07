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

}
