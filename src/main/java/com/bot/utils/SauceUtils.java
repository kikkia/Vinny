package com.bot.utils;

import com.kikkia.jsauce.SauceClient;

public class SauceUtils {

    private static SauceClient client;

    public static SauceClient getClient() {
        if (client == null) {
            SauceClient.Builder builder = new SauceClient.Builder();
            Config config = Config.getInstance();
            builder.addProxy(config.getConfig(Config.SAUCE_PROXY));
            builder.setToken(config.getConfig(Config.SAUCE_TOKEN));
            client = builder.build();
        }
        return client;
    }
}
