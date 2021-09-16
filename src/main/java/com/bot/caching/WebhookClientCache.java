package com.bot.caching;

import club.minnced.discord.webhook.WebhookClient;
import com.bot.config.properties.CacheProperties;
import com.bot.utils.Logger;
import org.springframework.stereotype.Component;

@Component
public class WebhookClientCache {
    private final Logger logger;

    private CustomWebhookCache cache;

    private WebhookClientCache(CacheProperties cacheProperties) {
        cache = new CustomWebhookCache("webhook", cacheProperties.getWebhookMax(), cacheProperties.getWebhookMaxLife(),
                cacheProperties.getWebhookCleanInterval());

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