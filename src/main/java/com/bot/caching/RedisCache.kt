package com.bot.caching

import com.bot.utils.VinnyConfig
import redis.clients.jedis.JedisPooled
import java.util.concurrent.ScheduledThreadPoolExecutor

class RedisCache private constructor() {
    private val logger = com.bot.utils.Logger(this.javaClass.name)

    private var redis: JedisPooled? = null
    private var enabled = false
    private var executor: ScheduledThreadPoolExecutor? = null

    init {
        val config = VinnyConfig.instance()
        if (config.cachingConfig != null) {
            enabled = config.cachingConfig.enabled
            if (enabled) {
                val uri = "redis://" + config.cachingConfig.redisUser + ":" +
                        config.cachingConfig.redisPassword + "@" +
                        config.cachingConfig.redisUrl + ":" + config.cachingConfig.redisPort
                redis = JedisPooled(uri)
                executor = ScheduledThreadPoolExecutor(2)
                logger.info("Connected to redis")
            }
        }
    }

    fun putStrList(key: String, value: List<String>, ttl: Long) {
        executor!!.submit {
            try {
                redis!!.rpush(key, *value.toTypedArray())
                redis!!.expire(key, ttl)
            } catch (e: Exception) {
                logger.warning("Failed to store entry in cache", e)
            }
        }
    }

    fun getStrList(key: String) : List<String> {
        return redis!!.lrange(key, 0, -1)
    }

    fun putString(key: String, value: String, ttl: Long) {
        executor!!.submit {
            try {
                redis!!.setex(key, ttl, value)
            } catch (e: Exception) {
                logger.warning("Failed to store entry in cache", e)
            }
        }
    }

    fun getStr(key: String) : String? {
        return redis!!.get(key)
    }

    companion object {
        private var instance: RedisCache? = null
        fun getInstance(): RedisCache {
                if (instance == null) {
                    instance = RedisCache()
                }
                return instance!!
            }
    }
}