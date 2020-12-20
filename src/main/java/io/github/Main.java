package io.github;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;

import io.github.service.*;

public class Main {

    private static final String TEMP_DIR_PREFIX = "wikiquote";
    private static final String WIKIQUOTE_LOGO_FILE_NAME = "wikiquote.png";
    private static final String NOTIFICATION_CONFIG_FILE_NAME = "config-toast.xml";
    private static final String NOTIFICATION_SCRIPT_FILE_NAME = "notification.ps1";

    public static void main(String[] args) throws Exception {
        var resourcesDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        copyResourcesToTempDir(resourcesDir);
        showQuoteOfTheDay(resourcesDir);
        cleanTempDir(resourcesDir);
    }

    private static void copyResourcesToTempDir(Path tempDir) {
        Stream.of(WIKIQUOTE_LOGO_FILE_NAME, NOTIFICATION_CONFIG_FILE_NAME, NOTIFICATION_SCRIPT_FILE_NAME)
            .forEach(fileName -> {
                try (var in = Main.class.getClassLoader().getResourceAsStream(fileName)) {
                    Files.copy(in, tempDir.resolve(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private static NotificationService getNotificationService(Path config, Path script) {
        if (SystemUtils.IS_OS_LINUX)
            return new LinuxNotificationService();
        else if (SystemUtils.IS_OS_WINDOWS_10)
            return new Windows10NotificationService(config, script);

        var msg = String.format("Unsupported OS name: %s, version: %s", SystemUtils.OS_NAME, SystemUtils.OS_VERSION);
        throw new UnsupportedOperationException(msg);
    }

    private static void showQuoteOfTheDay(Path resourcesDir) {
        var icon = resourcesDir.resolve(WIKIQUOTE_LOGO_FILE_NAME);
        var config = resourcesDir.resolve(NOTIFICATION_CONFIG_FILE_NAME);
        var script = resourcesDir.resolve(NOTIFICATION_SCRIPT_FILE_NAME);
        var notificationService = getNotificationService(config, script);
        new QuoteServiceImpl(icon, notificationService).showQuoteOfTheDay();
    }

    private static void cleanTempDir(Path tempDir) throws IOException {
        try (var stream = Files.list(tempDir)) {
            stream.forEach(Main::deleteFile);
            deleteFile(tempDir);
        }
    }

    private static void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}