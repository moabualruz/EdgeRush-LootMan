package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("behavioral_actions")
data class BehavioralActionEntity(
    @Id
    val id: Long? = null,
    @Column("guild_id")
    val guildId: String,
    @Column("character_name")
    val characterName: String,
    @Column("action_type")
    val actionType: String,
    @Column("deduction_amount")
    val deductionAmount: Double,
    @Column("reason")
    val reason: String,
    @Column("applied_by")
    val appliedBy: String,
    @Column("applied_at")
    val appliedAt: LocalDateTime,
    @Column("expires_at")
    val expiresAt: LocalDateTime?,
    @Column("is_active")
    val isActive: Boolean = true,
)
