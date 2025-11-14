package com.edgerush.datasync.config

import org.springframework.boot.context.properties.bind.DefaultValue

data class WoWAuditProperties(
    @DefaultValue("https://wowaudit.com")
    val baseUrl: String,
    val guildProfileUri: String?,
    val apiKey: String?,
)
