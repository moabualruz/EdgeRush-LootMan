package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("discord_user_links")
data class DiscordUserLinkEntity(
    @Id
    val id: Long? = null,
    @Column("discord_user_id")
    val discordUserId: String,
    @Column("raider_id")
    val raiderId: Long,
    @Column("is_primary")
    val isPrimary: Boolean = false,
    @Column("linked_at")
    val linkedAt: Instant,
    @Column("linked_by")
    val linkedBy: String?,
)
