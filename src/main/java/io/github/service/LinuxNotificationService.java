package io.github.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LinuxNotificationService implements NotificationService {

    @Override
    public void sendNotification(String title, String content, Path icon) {
        checkZenityInstallation();
        runZenity(title, content, icon);
    }

    private void checkZenityInstallation() {
        try {
            var command = List.of("sh", "-c", "command -v zenity");
            var process = new ProcessBuilder(command).start();
            var exitValue = process.onExit().get().exitValue();
            if (exitValue != 0)
                throw new UnsupportedOperationException("Require zenity but not installed");
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void runZenity(String title, String content, Path icon) {
        try {
            var command = List.of(
                "zenity",
                "--notification",
                String.format("--text=%s%n%s", title, content),
                String.format("--window-icon=%s", icon.toAbsolutePath())
            );
            var process = new ProcessBuilder(command).inheritIO().start();
            process.onExit().get();
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}