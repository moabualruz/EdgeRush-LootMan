package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class UserEntity(
    @Id
    val id: Long? = null,
    @Column("discord_id")
    val discordId: String?,
    @Column("battlenet_id")
    val battlenetId: String?,
    @Column("username")
    val username: String,
    @Column("email")
    val email: String?,
    @Column("password_hash")
    val passwordHash: String?,
    @Column("avatar_url")
    val avatarUrl: String?,
    @Column("role")
    val role: String,
    @Column("guild_id")
    val guildId: String?,
    @Column("created_at")
    val createdAt: Instant,
    @Column("last_login")
    val lastLogin: Instant?,
)
