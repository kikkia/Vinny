package com.bot.caching;

import com.bot.metrics.MetricsManager;
import com.bot.utils.Logger;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;

/**
 * This is an in-memory cache for caching any type of objects. This can be used to cache things that may take a while to
 * get/make or are very commonly used.
 * TODO: Eventually we should shift this off to something like redis.
 */
public class Cache<V> {
    private static final Logger LOGGER = new Logger(GuildCache.class.getName());

    private final LRUMap cacheMap;
    private MetricsManager metricsManager;
    private String name;
    private int maxLifetime;
    private int maxSize;
    private int cleanupInterval;

    protected Cache(String name, int max, int maxLifetime, int cleanupInterval) {
        this.maxLifetime = maxLifetime;
        this.maxSize = max;
        this.cleanupInterval = cleanupInterval;

        this.name = name;
        cacheMap = new LRUMap(maxSize);
        metricsManager = MetricsManager.getInstance();

        // Starts a thread that will cleanup the cache every CHECK_INTERVAL seconds
        if (this.maxLifetime > 0 && this.cleanupInterval > 0) {

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(cleanupInterval * 1000);
                    } catch (InterruptedException ex) {
                    }
                    cleanup();
                }
            });

            t.setDaemon(true);
            t.start();
        }

    }

    @SuppressWarnings("unchecked")
    public void put(String key, V value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new CacheObject<>(value));
        }
    }

    @SuppressWarnings("unchecked")
    public V get(String key) {
        synchronized (cacheMap) {
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
                metricsManager.updateCacheSize(name, cacheMap.size(), cacheMap.maxSize());
            }
        }
    }

    public void remove(String key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    public void removeAll() {
        synchronized (cacheMap) {
            for (Object entry : cacheMap.keySet())
                cacheMap.remove(entry);
        }
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<String> deleteKey;

        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();

            deleteKey = new ArrayList<>();
            String key;
            CacheObject<V> cacheObject;

            while (itr.hasNext()) {
                key = (String) itr.next();
                cacheObject = (CacheObject<V>) itr.getValue();

                if (cacheObject != null && (now > ((maxLifetime * 1000) + cacheObject.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (String key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }

            Thread.yield();
        }

        LOGGER.info(name + " Cache cleanup complete. Removed " + deleteKey.size() + " stale objects. " + name);
    }
}
