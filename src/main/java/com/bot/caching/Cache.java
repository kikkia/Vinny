package com.bot.caching;

import com.bot.metrics.MetricsManager;
import com.bot.utils.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is an in-memory cache for caching any type of objects. This can be used to cache things that may take a while to
 * get/make or are very commonly used.
 * TODO: Eventually we should shift this off to something like redis.
 */
public class Cache<V> {
    private final Logger LOGGER = new Logger(this.getClass().getName());

    protected final ConcurrentHashMap cacheMap;
    private final MetricsManager metricsManager;
    private final String name;
    private final int maxIdleLifetime;
    private final int maxTotalLifetime = 600; // 10 Mins, JDA caching does timeout eventually, we want to refresh this before it craps out
    private final int maxSize;
    private final int cleanupInterval;
    private final ScheduledExecutorService executorService;

    protected Cache(String name, int max, int maxIdleLifetime, int cleanupInterval) {
        this.maxIdleLifetime = maxIdleLifetime;
        this.maxSize = max;
        this.cleanupInterval = cleanupInterval;

        this.name = name;
        cacheMap = new ConcurrentHashMap(maxSize);
        metricsManager = MetricsManager.Companion.getInstance();
        executorService = new ScheduledThreadPoolExecutor(1);

        // Starts a thread that will cleanup the cache every CHECK_INTERVAL seconds
        if (this.maxIdleLifetime > 0 && this.cleanupInterval > 0) {
            executorService.scheduleAtFixedRate(this::cleanup, cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
        }
        LOGGER.info(name + " cache initialized");
    }

    @SuppressWarnings("unchecked")
    public void put(String key, V value) {
        cacheMap.put(key, new CacheObject<>(value));
    }

    @SuppressWarnings("unchecked")
    public V get(String key) {
        CacheObject<V> cacheObject = (CacheObject<V>) cacheMap.get(key);

        try {
            if (cacheObject == null) {
                metricsManager.markCacheMiss(name);
                return null;
            } else {
                metricsManager.markCacheHit(name);
                cacheObject.lastAccessed = System.currentTimeMillis();
                return cacheObject.value;
            }
        } finally {
            metricsManager.updateCacheSize(name, cacheMap.size());
        }
    }

    public void remove(String key) {
        cacheMap.remove(key);
    }

    public void removeAll() {
        for (Object entry : cacheMap.keySet())
            cacheMap.remove(entry);
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<String> deleteKeys;

        deleteKeys = new ArrayList<>();
        CacheObject<V> cacheObject;

        for (Object key : cacheMap.keySet()) {
            cacheObject = (CacheObject<V>) cacheMap.get(key);

            if (cacheObject != null && (((now > ((maxIdleLifetime * 1000L) + cacheObject.lastAccessed))) || (now > (maxTotalLifetime * 1000) + cacheObject.addedTime))) {
                deleteKeys.add((String) key);
            }
        }

        for (String deleteKey : deleteKeys) {
            removeEntity(deleteKey);
        }

        LOGGER.info(name + " Cache cleanup complete. Removed " + deleteKeys.size() + " stale objects. " + name);
    }

    protected void removeEntity(String key) {
        cacheMap.remove(key);
    }
}
