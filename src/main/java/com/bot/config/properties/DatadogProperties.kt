package com.bot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "datadog")
open class DatadogProperties {
    var hostname: String? = null
    var identifier: String? = null
}