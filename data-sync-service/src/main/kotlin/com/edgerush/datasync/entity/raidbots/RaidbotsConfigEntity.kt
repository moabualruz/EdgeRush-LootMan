package com.edgerush.datasync.entity.raidbots

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("raidbots_config")
data class RaidbotsConfigEntity(
    @Id val guildId: String,
    val enabled: Boolean,
    val encryptedApiKey: String?,
    val configJson: String,
    val updatedAt: Instant,
)
