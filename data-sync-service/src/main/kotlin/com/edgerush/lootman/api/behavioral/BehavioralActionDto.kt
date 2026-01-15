package com.edgerush.lootman.api.behavioral

import com.edgerush.datasync.entity.BehavioralActionEntity
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * Request DTO for creating a behavioral action.
 */
data class CreateBehavioralActionRequest(
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    @field:NotBlank(message = "Action type is required")
    val actionType: String,
    @field:DecimalMin(value = "0.0", message = "Deduction amount must be non-negative")
    @field:DecimalMax(value = "1.0", message = "Deduction amount cannot exceed 1.0")
    val deductionAmount: Double,
    @field:NotBlank(message = "Reason is required")
    val reason: String,
    @field:NotBlank(message = "Applied by is required")
    val appliedBy: String,
    val expiresAt: LocalDateTime? = null,
)

/**
 * Request DTO for updating a behavioral action.
 */
data class UpdateBehavioralActionRequest(
    val reason: String? = null,
    val deductionAmount: Double? = null,
    val expiresAt: LocalDateTime? = null,
    val isActive: Boolean? = null,
)

/**
 * Response DTO for a behavioral action.
 */
data class BehavioralActionResponse(
    val id: Long,
    val guildId: String,
    val characterName: String,
    val actionType: String,
    val deductionAmount: Double,
    val reason: String,
    val appliedBy: String,
    val appliedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
) {
    companion object {
        fun from(entity: BehavioralActionEntity): BehavioralActionResponse =
            BehavioralActionResponse(
                id = entity.id ?: 0L,
                guildId = entity.guildId,
                characterName = entity.characterName,
                actionType = entity.actionType,
                deductionAmount = entity.deductionAmount,
                reason = entity.reason,
                appliedBy = entity.appliedBy,
                appliedAt = entity.appliedAt,
                expiresAt = entity.expiresAt,
                isActive = entity.isActive,
            )
    }
}

/**
 * Response DTO for checking if a behavioral action exists.
 */
data class BehavioralActionExistsResponse(
    val exists: Boolean,
)

/**
 * Response DTO for behavioral action count.
 */
data class BehavioralActionCountResponse(
    val count: Long,
)

/**
 * Response DTO for total deduction amount.
 */
data class TotalDeductionResponse(
    val totalDeduction: Double,
)
