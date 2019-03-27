package com.bot.caching;

import com.bot.models.InternalGuild;
import com.bot.utils.Config;
import com.bot.utils.Logger;


/**
 * This is an in-memory cache for caching guild preferences. Since we will be getting the preferences on all messages sent on all channels vinny can see,
 * this caching will be very important to help deal with the excessive load on the db.
 * TODO: Eventually we should shift this off to something like redis.
 */
public class GuildCache {
    private final Logger LOGGER;

    private static GuildCache instance;
    private Cache<InternalGuild> cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static GuildCache getInstance() {
        if (instance == null)
            instance = new GuildCache();
        return instance;
    }

    private GuildCache() {
        Config config = Config.getInstance();
        // Set or default the settings for the map
        MAX_SIZE = config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS) == null ? 500 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS));
        CACHE_OBJECT_LIFETIME = config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME) == null ? 1200 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME));
        CACHE_CHECK_INTERVAL = config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL) == null ? 300 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL));

        cache = new Cache<>("guild", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        LOGGER = new Logger(GuildCache.class.getName());

        LOGGER.info("Guild Cache successfully initialized.");
    }

    @SuppressWarnings("unchecked")
    public void put(String key, InternalGuild value) {
        cache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public InternalGuild get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }
}
