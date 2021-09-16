package com.bot.caching;

import com.bot.config.properties.CacheProperties;
import com.bot.models.InternalGuild;
import com.bot.utils.Logger;
import org.springframework.stereotype.Component;


/**
 * This is an in-memory cache for caching guild preferences. Since we will be getting the preferences on all messages sent on all channels vinny can see,
 * this caching will be very important to help deal with the excessive load on the db.
 * TODO: Eventually we should shift this off to something like redis.
 */
@Component
public class GuildCache {
    private final Logger LOGGER;

    private Cache<InternalGuild> cache;

    public GuildCache(CacheProperties cacheProperties) {
        // Set or default the settings for the map

        cache = new Cache<>("guild", cacheProperties.getGuildMax(), cacheProperties.getGuildCleanInterval(),
                cacheProperties.getGuildCleanInterval());

        LOGGER = new Logger(GuildCache.class.getName());
    }

    public void put(String key, InternalGuild value) {
        cache.put(key, value);
    }

    public InternalGuild get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {return cache.size();}
}
