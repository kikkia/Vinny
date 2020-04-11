package com.bot.caching;

import com.bot.models.MarkovModel;
import com.bot.utils.Config;
import com.bot.utils.Logger;


public class MarkovModelCache {
    private static final Logger LOGGER = new Logger(MarkovModelCache.class.getName());

    private static MarkovModelCache instance;
    private Cache<MarkovModel> cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static MarkovModelCache getInstance() {
        if (instance == null)
            instance = new MarkovModelCache();
        return instance;
    }

    private MarkovModelCache() {
        Config config = Config.getInstance();
        // Set or default the settings for the map
        MAX_SIZE = config.getConfig(Config.MARKOV_CACHE_MAX_ITEMS) == null ? 50 : Integer.parseInt(config.getConfig(Config.MARKOV_CACHE_MAX_ITEMS));
        CACHE_OBJECT_LIFETIME = config.getConfig(Config.MARKOV_CACHE_OBJECT_LIFETIME) == null ? 7200 : Integer.parseInt(config.getConfig(Config.MARKOV_CACHE_OBJECT_LIFETIME));
        CACHE_CHECK_INTERVAL = config.getConfig(Config.MARKOV_CACHE_CLEANUP_INTERVAL) == null ? 1200 : Integer.parseInt(config.getConfig(Config.MARKOV_CACHE_CLEANUP_INTERVAL));

        cache = new Cache<>("markov", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        LOGGER.info("Markov Cache successfully initialized.");
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
}
