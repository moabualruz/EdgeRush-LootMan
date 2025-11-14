package com.edgerush.datasync.entity.warcraftlogs

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("warcraft_logs_character_mappings")
data class WarcraftLogsCharacterMappingEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val wowauditName: String,
    val wowauditRealm: String,
    val warcraftLogsName: String,
    val warcraftLogsRealm: String,
    val createdAt: Instant,
    val createdBy: String?
)
