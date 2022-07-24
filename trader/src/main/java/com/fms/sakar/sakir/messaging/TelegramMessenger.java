package com.fms.sakar.sakir.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

@ApplicationScoped
public class TelegramMessenger {

    private static final String MESSSAGING_API_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    @Inject
    Properties properties;

    public synchronized void sendToTelegram(String text) {
        String apiToken = properties.getProperty("telegram.api.key");
        String chatId = properties.getProperty("telegram.chat.id");

        String urlString = String.format(MESSSAGING_API_URL, apiToken, chatId, text);

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (Exception ignored) {}
    }
}