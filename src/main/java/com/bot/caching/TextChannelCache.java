package com.bot.caching;

import com.bot.models.InternalTextChannel;
import com.bot.utils.Config;
import com.bot.utils.Logger;

public class TextChannelCache {
    private final Logger LOGGER;

    private static TextChannelCache instance;
    private final Cache<InternalTextChannel> cache;
    private final int MAX_SIZE;
    private final int CACHE_OBJECT_LIFETIME;
    private final int CACHE_CHECK_INTERVAL;

    public static TextChannelCache getInstance() {
        if (instance == null)
            instance = new TextChannelCache();
        return instance;
    }

    private TextChannelCache() {
        Config config = Config.getInstance();
        // Set or default the settings for the map
        MAX_SIZE = config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS) == null ? 500 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS));
        CACHE_OBJECT_LIFETIME = config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME) == null ? 1200 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME));
        CACHE_CHECK_INTERVAL = config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL) == null ? 300 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL));

        cache = new Cache<>("text_channel", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        LOGGER = new Logger(TextChannelCache.class.getName());
    }

    public void put(String key, InternalTextChannel value) {
        cache.put(key, value);
    }

    public InternalTextChannel get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {return cache.size();}
}
