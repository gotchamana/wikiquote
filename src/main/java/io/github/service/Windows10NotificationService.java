package io.github.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Windows10NotificationService implements NotificationService {

    private Path snoreToast;

    public Windows10NotificationService(Path snoreToast) {
        this.snoreToast = snoreToast;
    }

    @Override
    public void sendNotification(String title, String content, Path icon) {
        try {
            var command = List.of(
                "cmd.exe",
                "/C",
                String.format("%s -t %s -m %s -p %s -appid ''", snoreToast.toAbsolutePath(), title, content,
                    icon.toAbsolutePath())
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