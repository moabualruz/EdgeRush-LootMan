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
    val bannedBy: String, // Guild leader who applied the ban
    val bannedAt: LocalDateTime,
    val expiresAt: LocalDateTime?, // When the ban expires (null = permanent)
    val isActive: Boolean = true,
)
