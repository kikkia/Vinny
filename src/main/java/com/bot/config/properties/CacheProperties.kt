package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "cache")
class CacheProperties {
    var markovMax: Int = 50
    var markovMaxLife: Int = 7200
    var markovCleanInterval: Int = 1200
    var guildMax: Int = 1000
    var guildMaxLife: Int = 1200
    var guildCleanInterval: Int = 300
    var r34Max: Int = 400
    var r34MaxLife: Int = 660
    var r34CleanInterval: Int = 3600
    var redditMax: Int = 200
    var redditMaxLife: Int = 300
    var redditCleanInterval: Int = 600
    var e621Max: Int = 400
    var e621MaxLife: Int = 1200
    var e621CleanInterval: Int = 1800
    var webhookMax: Int = 500
    var webhookMaxLife: Int = 6600
    var webhookCleanInterval: Int = 36000
}