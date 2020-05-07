package com.bot.utils;

import com.kikkia.dislog.api.DislogClient;
import com.kikkia.dislog.models.LogLevel;

public class LoggerUtils {

    public static DislogClient client;

    public static DislogClient getClient() {
        if (client == null) {
            Config config = Config.getInstance();

            DislogClient.Builder builder = new DislogClient.Builder()
                    .setUsername("Not Vinny")
                    .addWebhook(LogLevel.DEBUG, config.getConfig(Config.DEBUG_WEBHOOK))
                    .addWebhook(LogLevel.INFO, config.getConfig(Config.INFO_WEBHOOK))
                    .addWebhook(LogLevel.WARN, config.getConfig(Config.WARN_WEBHOOK))
                    .addWebhook(LogLevel.ERROR, config.getConfig(Config.ERROR_WEBHOOK))
                    .setIdentifier(config.getConfig(Config.HOST_IDENTIFIER))
                    .printStackTrace(true);
            client = builder.build();
        }
        return client;
    }
}
