package com.bot.caching;

import com.bot.utils.Logger;
import com.bot.utils.VinnyConfig;

import java.util.List;

public class R34Cache {
    private final Logger logger;

    private static R34Cache instance;
    private final Cache<List<String>> cache;
    private final int MAX_SIZE;
    private final int CACHE_OBJECT_LIFETIME;
    private final int CACHE_CHECK_INTERVAL;
    private RedisCache redisCache;
    private VinnyConfig config;
    private boolean redisEnabled = false;

    public static R34Cache getInstance() {
        if (instance == null) {
            instance = new R34Cache();
        }
        return instance;
    }

    private R34Cache() {
        MAX_SIZE = 400;
        CACHE_CHECK_INTERVAL = 660;
        CACHE_OBJECT_LIFETIME = 3600;

        cache = new Cache<>("r34", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        logger = new Logger(this.getClass().getName());
        config = VinnyConfig.Companion.instance();
        if (config.getCachingConfig() != null) {
            redisEnabled = Boolean.TRUE.equals(config.getCachingConfig().getR34enabled());
            if (redisEnabled) {
                redisCache = RedisCache.Companion.getInstance();
            }
        }
    }

    public void put(String key, List<String> value) {
        cache.put(key, value);
        if (redisEnabled) {
            redisCache.putStrList(redisKey(key), value, (CACHE_OBJECT_LIFETIME - 10));
        }
    }

    public List<String> get(String key) {
        List<String> val = cache.get(key);
        if (redisEnabled && val == null) {
            List<String> retrievedValues = redisCache.getStrList(redisKey(key));
            if (!retrievedValues.isEmpty()) {
                cache.put(key, retrievedValues);
                return retrievedValues;
            }
        }

        return val;
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {return cache.size();}

    private String redisKey(String key) {
        return "r34Cache:" + key;
    }
}
