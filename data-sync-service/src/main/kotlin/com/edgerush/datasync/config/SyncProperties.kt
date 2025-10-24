package com.edgerush.datasync.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "sync")
data class SyncProperties(
    @DefaultValue("0 0 4 * * *")
    val cron: String,
    val wowaudit: WoWAudit
) {
    data class WoWAudit(
        @DefaultValue("https://api.wowaudit.com/v1")
        val baseUrl: String,
        val guildProfileUri: String?,
        val apiKey: String?
    )
}
