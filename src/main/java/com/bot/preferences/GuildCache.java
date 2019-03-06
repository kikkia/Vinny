package com.bot.preferences;

import com.bot.models.InternalGuild;
import com.bot.utils.Config;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This is an in-memory cache for caching guild preferences. Since we will be getting the preferences on all messages sent on all channels vinny can see,
 * this caching will be very important to help deal with the excessive load on the db.
 * TODO: Eventually we should shift this off to something like redis.
 */
public class GuildCache {
    private static final Logger LOGGER = Logger.getLogger(GuildCache.class.getName());

    private static GuildCache instance;
    private final LRUMap cacheMap;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;


    public static GuildCache getInstance() {
        if (instance == null)
            instance = new GuildCache();
        return instance;
    }

    private GuildCache() {
        Config config = Config.getInstance();
        // Set or default the settings for the map
        MAX_SIZE = config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS) == null ? 500 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_MAX_ITEMS));
        CACHE_OBJECT_LIFETIME = config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME) == null ? 600 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_OBJECT_LIFETIME));
        CACHE_CHECK_INTERVAL = config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL) == null ? 300 : Integer.parseInt(config.getConfig(Config.GUILD_PREFS_CACHE_CLEANUP_INTERVAL));

        cacheMap = new LRUMap(MAX_SIZE);

        // Starts a thread that will cleanup the cache every CHECK_INTERVAL seconds
        if (CACHE_OBJECT_LIFETIME > 0 && CACHE_CHECK_INTERVAL > 0) {

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(CACHE_CHECK_INTERVAL * 1000);
                    } catch (InterruptedException ex) {
                    }
                    cleanup();
                }
            });

            t.setDaemon(true);
            t.start();
        }

        LOGGER.info("Guild Cache successfully initialized.");
    }

    @SuppressWarnings("unchecked")
    public void put(String key, InternalGuild value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new GuildPreferencesCacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public InternalGuild get(String key) {
        synchronized (cacheMap) {
            GuildPreferencesCacheObject cacheObject = (GuildPreferencesCacheObject) cacheMap.get(key);

            if (cacheObject == null)
                return null;
            else {
                cacheObject.lastAccessed = System.currentTimeMillis();
                return cacheObject.value;
            }
        }
    }

    protected class GuildPreferencesCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public InternalGuild value;

        GuildPreferencesCacheObject(InternalGuild value) {
            this.value = value;
        }
    }

    public void remove(String key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    private void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<String> deleteKey;

        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();

            deleteKey = new ArrayList<>();
            String key;
            GuildPreferencesCacheObject cacheObject;

            while (itr.hasNext()) {
                key = (String) itr.next();
                cacheObject = (GuildPreferencesCacheObject) itr.getValue();

                if (cacheObject != null && (now > ((CACHE_OBJECT_LIFETIME * 1000) + cacheObject.lastAccessed))) {
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

        LOGGER.info("Guild Cache cleanup complete. Removed " + deleteKey.size() + " stale guilds.");
    }
}
