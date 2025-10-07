package org.example.helper;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ConsentHelper {

    public static void dismissGoogleConsent(WebDriver driver) {
        // 1) Çerezle atlatmayı dene
        try {
            // Aynı hostta olman gerekiyor; değilse kısa bir gezinti yap
            if (!driver.getCurrentUrl().contains("google.")) {
                driver.get("https://www.google.com/?hl=en");
            }
            // Basit YES+ değeri çoğu bölgede yeterli
            Cookie consent = new Cookie.Builder("CONSENT", "YES+")
                    .path("/")
                    .isHttpOnly(false)
                    .isSecure(true)
                    .build();
            driver.manage().addCookie(consent);
            driver.navigate().refresh();
            // Eğer popup görünmüyorsa işimiz bitti
            if (!isConsentVisible(driver)) return;
        } catch (Throwable ignore) { /* cookie tutmazsa normale geçeceğiz */ }

        // 2) Görünüyorsa — çoklu strateji (iframe + çoklu dil + JS click)
        List<By> selectors = Arrays.asList(
                // En sık (EN)
                By.xpath("//button[.//div[normalize-space()='Reject all' or normalize-space()='Reject All']]"),
                By.xpath("//div[normalize-space()='Reject all' or normalize-space()='Reject All']"),
                By.cssSelector("button[aria-label='Reject all'], div[role='button'][aria-label='Reject all']"),

                // Türkçe
                By.xpath("//button[.//div[normalize-space()='Tümünü reddet']]"),
                By.xpath("//div[normalize-space()='Tümünü reddet']"),

                // Diğer yaygın diller
                By.xpath("//button[.//div[normalize-space()='Aceptar todo' or normalize-space()='Alle ablehnen' or normalize-space()='Tout refuser']]"),
                By.xpath("//div[normalize-space()='Aceptar todo' or normalize-space()='Alle ablehnen' or normalize-space()='Tout refuser']"),

                // Accept fallback (Reject bulamazsa)
                By.xpath("//button[.//div[normalize-space()='Accept all' or normalize-space()='Accept All']]"),
                By.cssSelector("button[aria-label='Accept all'], div[role='button'][aria-label='Accept all']")
        );

        // Önce sayfada dene, sonra consent iframelerde dene
        if (tryClickAny(driver, selectors)) return;

        // Consent iframeleri (src veya name 'consent' geçen)
        for (WebElement frame : driver.findElements(By.cssSelector("iframe, iframe[name], iframe[src]"))) {
            String name = frame.getAttribute("name");
            String src = frame.getAttribute("src");
            if ((name != null && name.toLowerCase().contains("consent"))
                    || (src != null && src.toLowerCase().contains("consent"))) {
                try {
                    driver.switchTo().frame(frame);
                    if (tryClickAny(driver, selectors)) {
                        driver.switchTo().defaultContent();
                        return;
                    }
                } catch (Throwable ignored) {
                } finally {
                    driver.switchTo().defaultContent();
                }
            }
        }
    }

    private static boolean tryClickAny(WebDriver driver, List<By> selectors) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        for (By by : selectors) {
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                // Görünür değilse görünür olana kadar bekle
                wait.until(ExpectedConditions.elementToBeClickable(el));
                // Normal tık başarısız olursa JS ile tıkla
                try { el.click(); }
                catch (Throwable e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                return true;
            } catch (Throwable ignored) { /* sıradaki seçici */ }
        }
        return false;
    }

    private static boolean isConsentVisible(WebDriver driver) {
        try {
            return !driver.findElements(By.xpath("//*[contains(.,'Before you continue to Google') or contains(.,'Google’a devam etmeden önce') or contains(.,'Bevor Sie fortfahren') or contains(.,'Antes de continuar')]")).isEmpty();
        } catch (Throwable e) {
            return false;
        }
    }
}
