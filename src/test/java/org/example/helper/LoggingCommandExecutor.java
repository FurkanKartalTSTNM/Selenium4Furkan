package org.example.helper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Set;
import java.util.function.Supplier;

public class LoggingCommandExecutor implements CommandExecutor {

    private final CommandExecutor delegate;
    private volatile Supplier<WebDriver> driverSupplier; // screenshot için
    private volatile CommandJsonSink sink;               // json log için
    private final Set<String> noShot = Set.of("newSession", "quit");

    public LoggingCommandExecutor(URL remoteUrl) {
        this.delegate = new HttpCommandExecutor(remoteUrl);
    }

    // Entegrasyon noktaları:
    public void setDriverSupplier(Supplier<WebDriver> supplier) {
        this.driverSupplier = supplier;
    }
    public void setSink(CommandJsonSink sink) {
        this.sink = sink;
    }

    @Override
    public Response execute(Command command) throws IOException {
        long t0 = System.nanoTime();
        Response resp = null;
        IOException thrown = null;
        try {
            resp = delegate.execute(command);
            return resp;
        } catch (IOException e) {
            thrown = e;
            throw e;
        } finally {
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            String sid = command.getSessionId() != null ? command.getSessionId().toString() : "<no-session>";
            Integer status = resp != null ? resp.getStatus() : null;

            String screenshotPath = null;
            if (shouldCapture(command.getName())) {
                Supplier<WebDriver> sup = this.driverSupplier;
                if (sup != null) {
                    WebDriver d = null;
                    try {
                        d = sup.get();
                    } catch (Throwable ignore) {}
                    if (d != null) {
                        screenshotPath = ScreenshotUtil.take(d, command.getName());
                    }
                }
            }

            // JSON log’a yaz
            CommandJsonSink s = this.sink;
            if (s != null) {
                // HTTP method/URI’yi HttpCommandExecutor üzerinden alamıyoruz (Selenium public API kısıtı).
                s.write(
                        command.getName(),
                        sid,
                        Long.valueOf(status),
                        ms,
                        command.getParameters(),
                        null,           // httpMethod (HTTP yakalama yapılmıyor bu sade versiyonda)
                        null,           // uri
                        screenshotPath
                );
            }

            // Konsola özet
            System.out.printf("WD CMD %-28s | sid=%s | status=%s | %d ms | params=%s | shot=%s%n",
                    command.getName(), sid, String.valueOf(status), ms,
                    String.valueOf(command.getParameters()), screenshotPath != null ? "yes" : "no");
        }
    }

    private boolean shouldCapture(String name) {
        if (name == null) return false;
        // newSession/quit hariç hepsinde screenshot almak istersen bu satırı false'a çevir
        return !noShot.contains(name);
    }
}
