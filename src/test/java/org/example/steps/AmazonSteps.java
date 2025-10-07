package org.example.steps;

import com.thoughtworks.gauge.Step;
import org.example.driver.DriverManager;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class AmazonSteps {
    private final WebDriver driver = DriverManager.get();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    @Step("Amazon anasayfasına gidilir")
    public void openAmazon() {
        driver.get(DriverManager.baseUrl());
        closePopups();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("twotabsearchtextbox")));
    }

    @Step("<text> diye aratılır")
    public void search(String text) {
        WebElement searchBox = driver.findElement(By.id("twotabsearchtextbox"));
        searchBox.clear();
        searchBox.sendKeys(text);
        driver.findElement(By.id("nav-search-submit-button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.s-main-slot div.s-result-item[data-component-type='s-search-result']")));
    }

    @Step("Arama sonuçlarından <n>. ürün açılır")
    public void openNthProduct(int n) {
        List<WebElement> results = driver.findElements(
                By.cssSelector("div.s-main-slot div.s-result-item[data-component-type='s-search-result']"));
        if (results.size() < n)
            Assertions.fail("Yeterli sonuç yok. Bulunan: " + results.size());

        WebElement item = results.get(n - 1).findElement(By.cssSelector("h2 a"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", item);
        item.click();

        for (String h : driver.getWindowHandles())
            driver.switchTo().window(h);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("productTitle")));
    }

    @Step("Ürün başlığı <text> içermelidir")
    public void verifyTitleContains(String text) {
        String title = driver.findElement(By.id("productTitle")).getText().toLowerCase(Locale.ROOT);
        Assertions.assertTrue(title.contains(text.toLowerCase(Locale.ROOT)),
                "Ürün başlığı beklenen metni içermiyor: " + title);
    }

    @Step("Ürün sepete eklenir")
    public void addToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-button"))).click();
        closePopups();
    }

    @Step("Sepet adedi en az <count> olmalıdır")
    public void verifyCartCount(int count) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-cart-count")));
        int actual = Integer.parseInt(driver.findElement(By.id("nav-cart-count")).getText().trim());
        Assertions.assertTrue(actual >= count, "Sepette beklenenden az ürün var: " + actual);
    }

    private void closePopups() {
        try {
            driver.findElement(By.id("sp-cc-accept")).click(); // cookie
        } catch (Exception ignored) {}
        try {
            driver.findElement(By.id("attachSiNoCoverage")).click(); // warranty popup
        } catch (Exception ignored) {}
        try {
            driver.findElement(By.id("attach-close_sideSheet-link")).click(); // side sheet
        } catch (Exception ignored) {}
    }
}
