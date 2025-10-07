package org.example.driver;

import org.example.helper.CommandJsonSink;
import org.example.helper.LoggingCommandExecutor;
import org.example.helper.ScreenshotListener;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DriverManager {
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();
    private static final Properties props = new Properties();

    static {
        try (InputStream in = new FileInputStream("env/default/java.properties")) {
            props.load(in);
        } catch (Exception e) {
            System.err.println("⚠️ Properties file could not be loaded: " + e.getMessage());
        }
    }

    public static void start() {
        if (TL_DRIVER.get() != null) return;

        String browser = props.getProperty("browser", "chrome");
        boolean headless = Boolean.parseBoolean(props.getProperty("headless", "true"));
        long implicitWait = Long.parseLong(props.getProperty("implicitWaitSeconds", "5"));
        long pageLoadTimeout = Long.parseLong(props.getProperty("pageLoadTimeoutSeconds", "30"));
        String remoteUrl = props.getProperty("remoteUrl", "").trim();
        String jsonLogPath = props.getProperty("commandLogPath", "reports/commands/commands.json");

        try {
            WebDriver driver;

            if ("chrome".equalsIgnoreCase(browser)) {
                ChromeOptions options = new ChromeOptions();
                options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                if (headless) {
                    options.addArguments("--headless");
                    options.addArguments("--window-size=1920,1080");
                } else {
                    options.addArguments("--start-maximized");
                }
                options.addArguments(
                        "--disable-gpu",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-notifications",
                        "--disable-infobars",
                        "--remote-allow-origins=*",
                        "--lang=en-US"
                );
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("intl.accept_languages", "en-US,en");
                prefs.put("profile.default_content_setting_values.notifications", 2);
                prefs.put("profile.default_content_setting_values.cookies", 1);
                options.setExperimentalOption("prefs", prefs);

                if (!remoteUrl.isEmpty()) {
                    System.out.println("🌐 Remote Chrome WebDriver kullanılacak: " + remoteUrl);
                    LoggingCommandExecutor exec = new LoggingCommandExecutor(new URL(remoteUrl));
                    exec.setSink(new CommandJsonSink(jsonLogPath));

                    RemoteWebDriver raw = new RemoteWebDriver(exec, options);
                    exec.setDriverSupplier(() -> raw); // komut sonrası screenshot için driver referansı

                    driver = new EventFiringDecorator(new ScreenshotListener()).decorate(raw);
                } else {
                    System.out.println("💻 Lokal ChromeDriver kullanılacak");
                    WebDriver raw = new org.openqa.selenium.chrome.ChromeDriver(options);
                    driver = new EventFiringDecorator(new ScreenshotListener()).decorate(raw);
                }

            } else if ("firefox".equalsIgnoreCase(browser)) {
                FirefoxOptions options = new FirefoxOptions();
                options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                if (headless) {
                    options.addArguments("-headless");
                    options.addArguments("--width=1920", "--height=1080");
                } else {
                    // Firefox’ta gerçek maximize bazen gecikebilir; boyut veriyoruz:
                    options.addArguments("--width=1920", "--height=1080");
                }
                options.addPreference("intl.accept_languages", "en-US,en");
                options.addPreference("dom.webnotifications.enabled", false);
                options.addPreference("permissions.default.desktop-notification", 2);

                if (!remoteUrl.isEmpty()) {
                    System.out.println("🌐 Remote Firefox WebDriver kullanılacak: " + remoteUrl);
                    LoggingCommandExecutor exec = new LoggingCommandExecutor(new URL(remoteUrl));
                    exec.setSink(new CommandJsonSink(jsonLogPath));

                    RemoteWebDriver raw = new RemoteWebDriver(exec, options);
                    exec.setDriverSupplier(() -> raw);

                    driver = new EventFiringDecorator(new ScreenshotListener()).decorate(raw);
                } else {
                    System.out.println("💻 Lokal FirefoxDriver kullanılacak");
                    WebDriver raw = new org.openqa.selenium.firefox.FirefoxDriver(options);
                    driver = new EventFiringDecorator(new ScreenshotListener()).decorate(raw);
                }

            } else {
                throw new IllegalArgumentException("🚫 Desteklenmeyen browser: " + browser);
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            TL_DRIVER.set(driver);

        } catch (Exception e) {
            throw new RuntimeException("🚨 WebDriver başlatılamadı: " + e.getMessage(), e);
        }
    }

    public static WebDriver get() {
        if (TL_DRIVER.get() == null) start();
        return TL_DRIVER.get();
    }

    public static void stop() {
        WebDriver d = TL_DRIVER.get();
        if (d != null) {
            d.quit();
            TL_DRIVER.remove();
        }
    }

    public static String baseUrl() {
        return props.getProperty("baseUrl", "https://www.google.com");
    }
}
