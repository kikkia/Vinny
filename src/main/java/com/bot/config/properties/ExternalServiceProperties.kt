package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "external")
open class ExternalServiceProperties {
    var p90Token: String? = null
    var redditToken: String? = null
    var redditClient: String? = null
    var enableExternalBotApis: Boolean = false
    var botsForDiscordToken: String? = null
    var discordBotListToken: String? = null
    var discordBotOrgToken: String? = null
    var botsOnDiscordToken: String? = null
    var botsGGToken: String? = null
    var discordBoatsToken: String? = null
    var botlistSpaceToken: String? = null
    var divineBotlistToken: String? = null
    var mythicalListToken: String? = null
    var extremeListToken: String? = null
    var sauceProxy: String? = null
    var sauceToken: String? = null
    var twitchClientId: String? = null
}