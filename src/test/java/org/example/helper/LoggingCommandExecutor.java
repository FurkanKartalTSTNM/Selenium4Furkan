package org.example.helper;

import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;

public class LoggingCommandExecutor implements CommandExecutor {

    private final CommandExecutor delegate;

    public LoggingCommandExecutor(URL remoteUrl) {
        this.delegate = new HttpCommandExecutor(remoteUrl);
    }

    @Override
    public Response execute(Command command) throws IOException {
        long t0 = System.nanoTime();
        Response resp = null;
        IOException error = null;
        try {
            resp = delegate.execute(command);
            return resp;
        } catch (IOException e) {
            error = e;
            throw e;
        } finally {
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            String sid = command.getSessionId() != null ? command.getSessionId().toString() : "<no-session>";
            Integer status = resp != null ? resp.getStatus() : null;

            // Burada kendi loglayıcını çağırabilirsin:
            // buildCommandResultLogs(command, new Date(t0/1_000_000), resp, /* encoded request yok */);
            System.out.printf("WD CMD %-28s | sid=%s | status=%s | %d ms | params=%s%n",
                    command.getName(), sid, String.valueOf(status), ms, String.valueOf(command.getParameters()));
        }
    }
}
