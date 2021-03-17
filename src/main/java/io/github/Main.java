package io.github;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;

import io.github.service.*;

public class Main {

    private static final String TEMP_DIR_PREFIX = "wikiquote";
    private static final String WIKIQUOTE_LOGO_FILE_NAME = "wikiquote.png";
    private static final String SNORE_TOAST_FILE_NAME = "snoretoast-x86.exe";

    public static void main(String[] args) throws Exception {
        var resourcesDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        resourcesDir.toFile().deleteOnExit();
        copyResourcesToTempDir(resourcesDir);

        showQuoteOfTheDay(resourcesDir);
    }

    private static void copyResourcesToTempDir(Path tempDir) {
        Stream.of(WIKIQUOTE_LOGO_FILE_NAME, SNORE_TOAST_FILE_NAME)
            .forEach(fileName -> {
                try (var in = Main.class.getClassLoader().getResourceAsStream(fileName)) {
                    var resource = tempDir.resolve(fileName);
                    resource.toFile().deleteOnExit();
                    Files.copy(in, resource);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private static void showQuoteOfTheDay(Path resourcesDir) {
        var icon = resourcesDir.resolve(WIKIQUOTE_LOGO_FILE_NAME);
        var snoreToast = resourcesDir.resolve(SNORE_TOAST_FILE_NAME);
        var notificationService = getNotificationService(snoreToast);
        new QuoteServiceImpl(icon, notificationService).showQuoteOfTheDay();
    }

    private static NotificationService getNotificationService(Path snoreToast) {
        if (SystemUtils.IS_OS_LINUX)
            return new LinuxNotificationService();
        else if (SystemUtils.IS_OS_WINDOWS_10)
            return new Windows10NotificationService(snoreToast);

        var msg = String.format("Unsupported OS name: %s, version: %s", SystemUtils.OS_NAME, SystemUtils.OS_VERSION);
        throw new UnsupportedOperationException(msg);
    }
}