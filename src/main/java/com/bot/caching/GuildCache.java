package com.bot.caching;

import com.bot.models.InternalGuild;
import com.bot.utils.Logger;


/**
 * This is an in-memory cache for caching guild preferences. Since we will be getting the preferences on all messages sent on all channels vinny can see,
 * this caching will be very important to help deal with the excessive load on the db.
 * TODO: Eventually we should shift this off to something like redis.
 */
public class GuildCache {
    private final Logger LOGGER;

    private static GuildCache instance;
    private final Cache<InternalGuild> cache;
    private final int MAX_SIZE;
    private final int CACHE_OBJECT_LIFETIME;
    private final int CACHE_CHECK_INTERVAL;

    public static GuildCache getInstance() {
        if (instance == null)
            instance = new GuildCache();
        return instance;
    }

    private GuildCache() {
        // Set or default the settings for the map
        MAX_SIZE = 1000;
        CACHE_OBJECT_LIFETIME = 1200;
        CACHE_CHECK_INTERVAL = 300;

        cache = new Cache<>("guild", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

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
