package com.bot.caching;

import com.bot.config.properties.CacheProperties;
import com.bot.utils.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class R34Cache {
    private final Logger logger;

    private Cache<List<String>> cache;

    public R34Cache(CacheProperties cacheProperties) {
        cache = new Cache<>("r34", cacheProperties.getR34Max(), cacheProperties.getR34MaxLife(),
                cacheProperties.getR34CleanInterval());

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
