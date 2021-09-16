package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "voice")
open class VoiceProperties {
    var enableYTIpRoutingIPV4: Boolean = false
    var ipV4Block: String? = null
    var excludedV4Addresses: List<String> = listOf()
    var enableYTIpRoutingIPV6: Boolean = false
    var ipV6Block: String? = null
    var defaultSearchProvider: String? = "ytsearch:"
}