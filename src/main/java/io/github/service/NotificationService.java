package io.github.service;

import java.nio.file.Path;

public interface NotificationService {
    void sendNotification(String title, String content, Path icon);
}