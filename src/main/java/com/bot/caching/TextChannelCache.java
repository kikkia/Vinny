package com.bot.caching;

import com.bot.models.InternalTextChannel;
import com.bot.utils.VinnyConfig;

public class TextChannelCache {
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
        VinnyConfig config = VinnyConfig.Companion.instance();
        // Set or default the settings for the map
        MAX_SIZE =  500;
        CACHE_OBJECT_LIFETIME = 1200;
        CACHE_CHECK_INTERVAL = 300;

        cache = new Cache<>("text_channel", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);
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
