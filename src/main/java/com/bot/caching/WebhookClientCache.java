package com.bot.caching;

import club.minnced.discord.webhook.WebhookClient;
import com.bot.utils.Logger;

public class WebhookClientCache {
    private final Logger logger;

    private static WebhookClientCache instance;
    private CustomWebhookCache cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static WebhookClientCache getInstance() {
        if (instance == null) {
            instance = new WebhookClientCache();
        }
        return instance;
    }

    private WebhookClientCache() {
        MAX_SIZE = 400;
        CACHE_CHECK_INTERVAL = 6600;
        CACHE_OBJECT_LIFETIME = 36000;

        cache = new CustomWebhookCache("webhook", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        logger = new Logger(this.getClass().getName());
    }

    public void put(String key, WebhookClient value) {
        cache.put(key, value);
    }

    public WebhookClient get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {
        return cache.size();
    }

    public class CustomWebhookCache extends Cache<WebhookClient> {

        CustomWebhookCache(String name, int max, int maxIdleLifetime, int cleanupInterval) {
            super(name, max, maxIdleLifetime, cleanupInterval);
        }

        @Override
        protected void removeEntity(String key) {
            try {
                CacheObject<WebhookClient> object = (CacheObject<WebhookClient>) cacheMap.get(key);
                object.value.close();
            } catch (Exception e) {
                logger.warning("Exception cleaning from webhook cache", e);
            }
            cacheMap.remove(key);
        }
    }
}