package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "nats")
open class NatsProperties {
    var enable: Boolean = true
    var severAddress: String? = null
    var token: String? = null
    var subject: String? = null
}