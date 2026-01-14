package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("behavioral_actions")
data class BehavioralActionEntity(
    @Id
    val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val actionType: String,
    val deductionAmount: Double,
    val reason: String,
    val appliedBy: String,
    val appliedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean = true,
)
