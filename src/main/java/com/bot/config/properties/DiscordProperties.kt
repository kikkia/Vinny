package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "discord")
open class DiscordProperties {
    var token: String? = null
    var ownerId: String? = null
    var botId: String? = null
    var silent: Boolean = false
    var prefix: String = "~"
    var dataLoader: Boolean = false
    var enableLoggingChannels: Boolean = false
    var errorWebhook: String? = null
    var warningWebhook: String? = null
    var infoWebhook: String? = null
    var debugWebhook: String? = null
    var onlineEmoji: String = "<:online:561655473179459614>"
    var idleEmoji: String = "<:idle:561655480963956758>"
    var dndEmoji: String = "<:dnd:561655462991233054>"
    var offlineEmoji: String = "<:offline:561655488006062098>"
    var totalShards: Int = 1
    var startShard: Int = 0
    var endShard: Int = totalShards - 1
    var enableScheduledCommands: Boolean = true
}