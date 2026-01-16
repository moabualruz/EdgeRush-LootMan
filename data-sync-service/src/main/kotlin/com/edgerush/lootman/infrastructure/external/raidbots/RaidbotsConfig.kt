package com.edgerush.lootman.infrastructure.external.raidbots

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "raidbots")
data class RaidbotsConfig(
    var url: String = "https://www.raidbots.com",
    var apiKey: String = "",
    var enabled: Boolean = false
)
