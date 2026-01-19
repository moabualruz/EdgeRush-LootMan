package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("loot_bans")
data class LootBanEntity(
    @Id
    val id: Long? = null,
    @Column("guild_id")
    val guildId: String,
    @Column("character_name")
    val characterName: String,
    @Column("reason")
    val reason: String,
    @Column("banned_by")
    val bannedBy: String,
    @Column("banned_at")
    val bannedAt: LocalDateTime,
    @Column("expires_at")
    val expiresAt: LocalDateTime?,
    @Column("is_active")
    val isActive: Boolean = true,
)
