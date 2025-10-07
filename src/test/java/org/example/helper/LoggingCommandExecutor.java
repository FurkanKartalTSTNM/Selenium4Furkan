package org.example.helper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.function.Supplier;

public class LoggingCommandExecutor implements CommandExecutor {

    // Screenshot/rapor dışında tutulacak komutlar
    private static final Set<String> IGNORED_COMMANDS = Set.of(
            "getScreenshot", "getPageSource", "executeScript",
            "getElementText", "getTitle", "getWindowHandles",
            "getWindowHandle", "setTimeouts", "getTimeouts"
    );

    // Screenshot almaya gerek olmayan komutlar
    private static final Set<String> NO_SHOT = Set.of(
            "newSession", "quit", "screenshot", "getElementScreenshot",
            "actions", "setTimeouts", "getTimeouts", "getTitle",
            "getPageSource", "getWindowHandle", "getWindowHandles"
    );

    private final CommandExecutor delegate;
    private volatile Supplier<WebDriver> driverSupplier; // screenshot için
    private volatile CommandJsonSink sink;               // json log için

    public LoggingCommandExecutor(URL remoteUrl) {
        this.delegate = new HttpCommandExecutor(remoteUrl);
    }

    // entegrasyon noktaları
    public void setDriverSupplier(Supplier<WebDriver> supplier) { this.driverSupplier = supplier; }
    public void setSink(CommandJsonSink sink) { this.sink = sink; }

    @Override
    public Response execute(Command command) throws IOException {
        final long t0 = System.nanoTime();
        Response resp = null;
        IOException thrown = null;

        try {
            resp = delegate.execute(command);
            return resp;
        } catch (IOException e) {
            thrown = e;            // finally’da hata olarak işaretleriz
            throw e;
        } finally {
            final long ms = (System.nanoTime() - t0) / 1_000_000L;
            final String cmdName = command.getName();
            final String sid = command.getSessionId() != null ? command.getSessionId().toString() : "<no-session>";

            Integer status = (resp != null ? resp.getStatus() : null);
            String level = (thrown != null) ? "ERROR" : (status != null && status == 0 ? "PASS" : "INFO");

            // Screenshot (yalnızca gerekli komutlarda)
            String screenshotPath = null;
            if (shouldCapture(cmdName)) {
                final Supplier<WebDriver> sup = this.driverSupplier;
                if (sup != null) {
                    try {
                        WebDriver d = sup.get();
                        if (d != null) {
                            screenshotPath = ScreenshotUtil.take(d, safeName(cmdName));
                        }
                    } catch (Throwable ignore) { /* ekran görüntüsü isteğe bağlı */ }
                }
            }

            // JSON sink’e yaz
            final CommandJsonSink s = this.sink;
            if (s != null && !IGNORED_COMMANDS.contains(cmdName)) {
                // HTTP method/URI Selenium public API’den alınamıyor; null bırakıyoruz.
                s.write(
                        cmdName,                      // requestPath
                        sid,                          // session id
                        status == null ? null : status.longValue(),
                        ms,                           // runtime (ms)
                        command.getParameters(),      // requestData
                        null,                         // httpMethod
                        null,                         // uri
                        screenshotPath                // screenshotName
                );
            }

            System.out.printf("WD CMD %-28s | sid=%s | level=%s | status=%s | %d ms | shot=%s%n",
                    cmdName, sid, level, String.valueOf(status), ms,
                    (screenshotPath != null ? "yes" : "no"));
        }
    }

    private boolean shouldCapture(String name) {
        if (name == null) return false;
        return !(NO_SHOT.contains(name) || IGNORED_COMMANDS.contains(name));
    }

    private static String safeName(String s) {
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
