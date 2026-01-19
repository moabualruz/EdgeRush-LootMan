package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("enhanced_applications")
data class RecruitmentApplicationEntity(
    @Id
    @Column("enhanced_application_id")
    val id: String,

    @Column("guild_id")
    val guildId: String,

    @Column("battle_net_id")
    val battleNetId: String,

    @Column("discord_id")
    val discordId: String,

    @Column("email")
    val email: String,

    // Character Data
    @Column("character_name")
    val characterName: String,

    @Column("character_realm")
    val characterRealm: String,

    @Column("character_class")
    val characterClass: String,

    @Column("specialization")
    val specialization: String,

    @Column("item_level")
    val itemLevel: Double,

    @Column("raider_io_score")
    val raiderIoScore: Double?,

    @Column("best_parse_average")
    val bestParseAverage: Double?,

    // User Input
    @Column("age")
    val age: Int,
    @Column("location")
    val location: String,
    @Column("timezone")
    val timezone: String,

    @Column("raid_days_available")
    val raidDaysAvailable: String, // JSON array stored as string

    @Column("previous_guilds")
    val previousGuilds: String,

    @Column("reason_for_leaving")
    val reasonForLeaving: String,

    @Column("why_this_guild")
    val whyThisGuild: String,

    // Status
    @Column("status")
    val status: String,

    @Column("reviewed_by")
    val reviewedBy: String?,

    @Column("reviewed_at")
    val reviewedAt: OffsetDateTime?,

    @Column("created_at")
    val createdAt: OffsetDateTime,

    @Column("updated_at")
    val updatedAt: OffsetDateTime,
)
