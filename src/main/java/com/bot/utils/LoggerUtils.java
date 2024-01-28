package com.bot.utils;

import com.kikkia.dislog.api.DislogClient;
import com.kikkia.dislog.models.LogLevel;

public class LoggerUtils {

    public static DislogClient client;

    public static DislogClient getClient() {
        if (client == null) {
            VinnyConfig config = VinnyConfig.Companion.instance();

            DislogClient.Builder builder = new DislogClient.Builder()
                    .setUsername("Not Vinny")
                    .setIdentifier(config.getBotConfig().getHostIdentifier())
                    .printStackTrace(true);

            for(String hook : config.getBotConfig().getDebugWebhooks()) {
                builder.addWebhook(LogLevel.DEBUG, hook);
            }
            for(String hook : config.getBotConfig().getInfoWebhooks()) {
                builder.addWebhook(LogLevel.INFO, hook);
            }
            for(String hook : config.getBotConfig().getWarningWebhooks()) {
                builder.addWebhook(LogLevel.WARN, hook);
            }
            for(String hook : config.getBotConfig().getErrorWebhooks()) {
                builder.addWebhook(LogLevel.ERROR, hook);
            }
            client = builder.build();
        }
        return client;
    }
}
