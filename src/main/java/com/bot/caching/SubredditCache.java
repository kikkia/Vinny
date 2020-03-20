package com.bot.caching;

import com.bot.utils.Logger;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import java.util.List;

public class SubredditCache {
    private final Logger LOGGER;

    private static SubredditCache instance;
    private Cache<List<Listing<Submission>>> cache;
    private int MAX_SIZE;
    private int CACHE_OBJECT_LIFETIME;
    private int CACHE_CHECK_INTERVAL;

    public static SubredditCache getInstance() {
        if (instance == null) {
            instance = new SubredditCache();
        }
        return instance;
    }

    private SubredditCache() {
        // Just doing some default values here
        MAX_SIZE = 200;
        CACHE_CHECK_INTERVAL = 300;
        CACHE_OBJECT_LIFETIME = 600;

        cache = new Cache<>("subreddit", MAX_SIZE, CACHE_OBJECT_LIFETIME, CACHE_CHECK_INTERVAL);

        LOGGER = new Logger(SubredditCache.class.getName());
        LOGGER.info("subreddit Cache initialized.");
    }

    public void put(String key, List<Listing<Submission>> value) {
        cache.put(key, value);
    }

    public List<Listing<Submission>> get(String key) {
        return cache.get(key);
    }

    public void removeAll() {
        cache.removeAll();
    }

    public int getSize() {return cache.size();}
}
