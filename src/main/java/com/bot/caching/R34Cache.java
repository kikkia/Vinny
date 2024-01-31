package com.bot.caching;

import com.bot.utils.Logger;
import com.bot.utils.VinnyConfig;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class R34Cache {
    private final Logger logger;

    private static R34Cache instance;
    private final Cache<List<String>> cache;
    private final int MAX_SIZE;
    private final int CACHE_OBJECT_LIFETIME;
    private final int CACHE_CHECK_INTERVAL;
    private JedisPooled redisConn;
    private VinnyConfig config;
    private ExecutorService redisThreadPool;
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
            redisEnabled = config.getCachingConfig().getR34enabled();
            if (redisEnabled) {
                String uri = "redis://" + config.getCachingConfig().getRedisUser() + ":" +
                        config.getCachingConfig().getRedisPassword() + "@" +
                        config.getCachingConfig().getRedisUrl() + ":" + config.getCachingConfig().getRedisPort();
                redisConn = new JedisPooled(uri);
                redisThreadPool = new ScheduledThreadPoolExecutor(2);
                logger.info("Connected to r34 redis");
            }
        }
    }

    public void put(String key, List<String> value) {
        cache.put(key, value);
        if (redisEnabled) {
            redisThreadPool.submit(() -> {
                try {
                    redisConn.rpush(redisKey(key), value.toArray(new String[0]));
                    redisConn.expire(redisKey(key), CACHE_OBJECT_LIFETIME);
                } catch(Exception e) {
                   logger.warning("Failed to store entry in cache", e);
                }
            });
        }
    }

    public List<String> get(String key) {
        List<String> val = cache.get(key);
        if (redisEnabled && val == null) {
            List<String> retrievedValues = redisConn.lrange(redisKey(key), 0, -1);
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
