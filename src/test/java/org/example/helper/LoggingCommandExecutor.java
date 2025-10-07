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

    private static final Set<String> IGNORED_COMMANDS = Set.of(
            "getScreenshot","getPageSource","executeScript",
            "getElementText","getTitle","getWindowHandles",
            "getWindowHandle","setTimeouts","getTimeouts"
    );

    private static final Set<String> NO_SHOT = Set.of(
            "newSession","quit","screenshot","getElementScreenshot",
            "actions","setTimeouts","getTimeouts","getTitle",
            "getPageSource","getWindowHandle","getWindowHandles"
    );

    private final CommandExecutor delegate;
    private volatile Supplier<WebDriver> driverSupplier;
    private volatile CommandJsonSink sink; // opsiyonel

    public LoggingCommandExecutor(URL remoteUrl) {
        this.delegate = new HttpCommandExecutor(remoteUrl);
    }

    public void setDriverSupplier(Supplier<WebDriver> supplier) { this.driverSupplier = supplier; }
    public void setSink(CommandJsonSink sink) { this.sink = sink; }

    @Override
    public Response execute(Command command) throws IOException {
        final long t0ns = System.nanoTime();
        Response resp = null;
        IOException thrown = null;

        try {
            resp = delegate.execute(command);
            return resp;
        } catch (IOException e) {
            thrown = e;
            throw e;
        } finally {
            final long elapsedMs = (System.nanoTime() - t0ns) / 1_000_000L;
            final long startMs   = System.currentTimeMillis() - elapsedMs;

            final String cmdName = command.getName();
            final String sid     = command.getSessionId() != null ? command.getSessionId().toString() : "<no-session>";
            final Integer status = (resp != null ? resp.getStatus() : null);
            final String level   = (thrown != null) ? "ERROR" : (status != null && status == 0 ? "PASS" : "INFO");

            String screenshotPath = null;
            if (shouldCapture(cmdName)) {
                Supplier<WebDriver> sup = this.driverSupplier;
                if (sup != null) {
                    try {
                        WebDriver d = sup.get();
                        if (d != null) screenshotPath = ScreenshotUtil.take(d, safeName(cmdName));
                    } catch (Throwable ignore) {}
                }
            }

            // ----> 1) Collector’a ekle (JSON garantisi)
            if (!IGNORED_COMMANDS.contains(cmdName)) {
                CommandResultLog log = new CommandResultLog();
                log.setScreenshotName(screenshotPath);
                log.setMethod(null); // Selenium public API'den kolay erişim yok
                log.setRequestData(String.valueOf(command.getParameters()));
                log.setResponseData(status == null ? null : String.valueOf(status));
                log.setRequestPath(cmdName);
                log.setStartDate(startMs);
                log.setRuntime((int) elapsedMs);
                log.setEndDate(startMs + elapsedMs);
                log.setLevel(level);
                CommandResultCollector.add(log);
            }

            // ----> 2) (Opsiyonel) Ek bir sink kullanıyorsan
            CommandJsonSink s = this.sink;
            if (s != null && !IGNORED_COMMANDS.contains(cmdName)) {
                s.write(cmdName, sid, status == null ? null : status.longValue(),
                        elapsedMs, command.getParameters(), null, null, screenshotPath);
            }

            System.out.printf("WD CMD %-28s | sid=%s | level=%s | status=%s | %d ms | shot=%s%n",
                    cmdName, sid, level, String.valueOf(status), elapsedMs,
                    (screenshotPath != null ? "yes" : "no"));
        }
    }

    private boolean shouldCapture(String name) {
        return name != null && !(NO_SHOT.contains(name) || IGNORED_COMMANDS.contains(name));
    }

    private static String safeName(String s) {
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
