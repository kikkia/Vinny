package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "db")
open class DatabaseProperties {
    var uri: String? = null
    var username: String? = null
    var password: String? = null
    var schema: String? = null
    var maxPoolSize: Int = 50
    var minPoolIdle: Int = 2
}