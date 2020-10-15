package com.bot.caching;

import com.bot.utils.Logger;

import java.util.List;

public class E621Cache {
    private final Logger logger;

    private static E621Cache instance;
    private Cache<List<String>> cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static E621Cache getInstance() {
        if (instance == null) {
            instance = new E621Cache();
        }
        return instance;
    }

    private E621Cache() {
        MAX_SIZE = 400;
        CACHE_CHECK_INTERVAL = 661;
        CACHE_OBJECT_LIFETIME = 3600;

        cache = new Cache<>("e621", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        logger = new Logger(this.getClass().getName());
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
