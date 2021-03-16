package io.github.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Pattern;

public class QuoteServiceImpl implements QuoteService {

    private static final String WIKIQUOTE_URL = "https://zh.wikiquote.org/wiki/Wikiquote:%E9%A6%96%E9%A1%B5";

    private Path icon;
    private NotificationService notificationService;

    public QuoteServiceImpl(Path icon, NotificationService notificationService) {
        this.icon = icon;
        this.notificationService = notificationService;
    }

    @Override
    public void showQuoteOfTheDay() {
        try {
            var quote = getQuote();
            notificationService.sendNotification("維基語錄", quote, icon);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private String getQuote() throws IOException, InterruptedException {
        var uri = URI.create(WIKIQUOTE_URL + "?" + getRandomAlphanumeric(5));
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri)
            .header("Accept", "text/html")
            .header("Accept-Language", "zh-TW")
            .build();
        var response = client.send(request, BodyHandlers.ofString());

        return parseQuoteFromHtml(response.body());
    }

    private String getRandomAlphanumeric(int length) {
        final var ALPHANUMERIC = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var rlt = new StringBuilder();

        new Random().ints(length, 0, ALPHANUMERIC.length())
            .map(ALPHANUMERIC::charAt)
            .forEach(ch -> rlt.append((char) ch));

        return rlt.toString();
    }

    private String parseQuoteFromHtml(String html) {
        var quote = "";
        var regex = "<div id=\"mp-everyday-quote\">.*?<td>(.*?)</td>.*?</div>";
        var pattern = Pattern.compile(regex, Pattern.DOTALL);
        var matcher = pattern.matcher(html);

        if (!matcher.find()) return quote;

        var quoteWithAnchor = matcher.group(1);
        regex = "(.*?)<a.*?>(.*?)</a>(.*?)";
        pattern = Pattern.compile(regex, Pattern.DOTALL);
        matcher = pattern.matcher(quoteWithAnchor);

        if (matcher.find())
            quote = matcher.group(1) + matcher.group(2) + matcher.group(3);

        return quote;
    }
}