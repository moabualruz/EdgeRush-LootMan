package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("loot_bans")
data class LootBanEntity(
    @Id
    val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val reason: String,
    val bannedBy: String,
    val bannedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean = true,
)
