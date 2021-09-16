package com.bot.caching;

import com.bot.config.properties.CacheProperties;
import com.bot.models.MarkovModel;
import com.bot.utils.Logger;
import org.springframework.stereotype.Component;

@Component
public class MarkovModelCache {
    private static final Logger LOGGER = new Logger(MarkovModelCache.class.getName());
    private Cache<MarkovModel> cache;

    public MarkovModelCache(CacheProperties cacheProperties) {
        cache = new Cache<>("markov", cacheProperties.getMarkovMax(), cacheProperties.getMarkovMaxLife(),
                cacheProperties.getMarkovCleanInterval());
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

    public void remove(String key) {
        cache.remove(key);
    }
}
