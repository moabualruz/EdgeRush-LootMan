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
    val actionType: String, // "DEDUCTION", "RESTORATION"
    val deductionAmount: Double, // Amount to deduct from behavioral score (0.0 to 1.0)
    val reason: String,
    val appliedBy: String, // Guild leader who applied the action
    val appliedAt: LocalDateTime,
    val expiresAt: LocalDateTime?, // When the deduction expires (null = permanent)
    val isActive: Boolean = true,
)
