package com.bot.caching;

import com.bot.models.MarkovModel;


public class MarkovModelCache {
    private static MarkovModelCache instance;
    private final Cache<MarkovModel> cache;
    private final int MAX_SIZE;
    private final int CACHE_OBJECT_LIFETIME;
    private final int CACHE_CHECK_INTERVAL;

    public static MarkovModelCache getInstance() {
        if (instance == null)
            instance = new MarkovModelCache();
        return instance;
    }

    private MarkovModelCache() {
        // Set or default the settings for the map
        MAX_SIZE = 50;
        CACHE_OBJECT_LIFETIME = 7200;
        CACHE_CHECK_INTERVAL = 1200;

        cache = new Cache<>("markov", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);
    }

    public void put(String key, MarkovModel value) {
        cache.put(key, value);
    }

    public MarkovModel get(String key) {
        return cache.get(key);
    }

    public int getSize() {return cache.size();}

    public void removeAll() {
        cache.removeAll();
    }

    public void remove(String key) {
        cache.remove(key);
    }
}
