package com.bot.caching;

import com.bot.config.properties.CacheProperties;
import com.bot.utils.Logger;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubredditCache {
    private final Logger LOGGER;

    private Cache<List<Listing<Submission>>> cache;


    public SubredditCache(CacheProperties cacheProperties) {
        cache = new Cache<>("subreddit", cacheProperties.getRedditMax(), cacheProperties.getRedditMaxLife(),
                cacheProperties.getRedditCleanInterval());

        LOGGER = new Logger(SubredditCache.class.getName());
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
