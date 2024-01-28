package com.bot.utils;

import com.kikkia.jsauce.SauceClient;

public class SauceUtils {

    private static SauceClient client;

    public static SauceClient getClient() {
        if (client == null) {
            SauceClient.Builder builder = new SauceClient.Builder();
            VinnyConfig config = VinnyConfig.Companion.instance();
            builder.addProxy(config.getThirdPartyConfig().getSauceProxy());
            builder.setToken(config.getThirdPartyConfig().getSauceToken());
            client = builder.build();
        }
        return client;
    }
}
