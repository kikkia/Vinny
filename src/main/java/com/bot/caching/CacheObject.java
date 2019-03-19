package com.bot.caching;

public class CacheObject<V> {
    public long lastAccessed = System.currentTimeMillis();
    public long addedTime = System.currentTimeMillis();
    public V value;

    CacheObject(V value) {
        this.value = value;
    }
}
