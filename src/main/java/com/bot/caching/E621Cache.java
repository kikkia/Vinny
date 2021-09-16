package com.bot.caching;

import com.bot.config.properties.CacheProperties;
import com.bot.utils.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class E621Cache {
    private final Logger logger;

    private Cache<List<String>> cache;

    public E621Cache(CacheProperties cacheProperties) {
        cache = new Cache<>("e621", cacheProperties.getE621Max(), cacheProperties.getE621MaxLife(),
                cacheProperties.getE621CleanInterval());

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
