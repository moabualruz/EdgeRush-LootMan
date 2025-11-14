package com.edgerush.datasync.entity.warcraftlogs

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("warcraft_logs_config")
data class WarcraftLogsConfigEntity(
    @Id val guildId: String,
    val enabled: Boolean,
    val guildName: String,
    val realm: String,
    val region: String,
    val encryptedClientId: String?,
    val encryptedClientSecret: String?,
    val configJson: String,
    val updatedAt: Instant,
    val updatedBy: String?
)
