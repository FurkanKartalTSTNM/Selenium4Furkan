package org.example.steps;


import com.thoughtworks.gauge.Step;
import org.example.driver.DriverManager;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class StableSteps {
    private WebDriver d() { return DriverManager.get(); }
    private WebDriverWait w(long s) { return new WebDriverWait(d(), Duration.ofSeconds(s)); }

    // ---------- Genel ----------

}
