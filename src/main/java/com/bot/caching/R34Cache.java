package com.bot.caching;

import com.bot.utils.Logger;

import java.util.List;

public class R34Cache {
    private final Logger logger;

    private static R34Cache instance;
    private Cache<List<String>> cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static R34Cache getInstance() {
        if (instance == null) {
            instance = new R34Cache();
        }
        return instance;
    }

    private R34Cache() {
        MAX_SIZE = 100;
        CACHE_CHECK_INTERVAL = 600;
        CACHE_OBJECT_LIFETIME = 1200;

        cache = new Cache<>("r34", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        logger = new Logger(this.getClass().getName());
        logger.info("r34 cache initialized");
    }

    public void put(String key, List<String> value) {
        cache.put(key, value);
    }

    public List<String> get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {return cache.size();}
}
