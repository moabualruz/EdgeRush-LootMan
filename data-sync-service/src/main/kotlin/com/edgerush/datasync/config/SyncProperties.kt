package com.edgerush.datasync.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "sync")
data class SyncProperties(
    @DefaultValue("0 0 4 * * *")
    val cron: String,
    @DefaultValue("false")
    val runOnStartup: Boolean,
    val wowaudit: WoWAuditProperties,
)
