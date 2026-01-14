package com.edgerush.lootman.api.admin

import com.edgerush.lootman.api.behavioral.BehavioralActionResponse
import com.edgerush.lootman.api.loot.LootBanResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Request DTO for creating a behavioral action via admin API.
 * Maps to frontend BehavioralAction type.
 */
data class AdminBehavioralActionRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,

    @field:NotBlank(message = "Character name is required")
    val characterName: String,

    @field:NotBlank(message = "Action type is required")
    val actionType: String, // PENALTY or BONUS

    @field:NotBlank(message = "Reason is required")
    val reason: String,

    @field:NotNull(message = "FLPS modifier is required")
    val flpsModifier: Double,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date string

    val endDate: String? = null, // ISO date string
)

/**
 * Request DTO for updating a behavioral action via admin API.
 */
data class AdminBehavioralActionUpdateRequest(
    val reason: String? = null,
    val flpsModifier: Double? = null,
    val endDate: String? = null, // ISO date string or null to clear
    val active: Boolean? = null,
)

/**
 * Response DTO for behavioral action via admin API.
 * Maps to frontend BehavioralAction type.
 */
data class AdminBehavioralActionResponse(
    val id: Long,
    val raiderId: Long,
    val characterName: String,
    val actionType: String,
    val reason: String,
    val flpsModifier: Double,
    val startDate: String, // ISO date string
    val endDate: String?, // ISO date string
    val createdBy: String,
    val active: Boolean,
) {
    companion object {
        fun from(response: BehavioralActionResponse): AdminBehavioralActionResponse {
            return AdminBehavioralActionResponse(
                id = response.id,
                raiderId = 0L, // Backend doesn't track raiderId for behavioral actions
                characterName = response.characterName,
                actionType = response.actionType,
                reason = response.reason,
                flpsModifier = -response.deductionAmount, // Convert deduction to modifier (negative)
                startDate = response.appliedAt.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                endDate = response.expiresAt?.atOffset(ZoneOffset.UTC)?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                createdBy = response.appliedBy,
                active = response.isActive,
            )
        }
    }
}

/**
 * Request DTO for creating a loot ban via admin API.
 * Maps to frontend LootBan type.
 */
data class AdminLootBanRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,

    @field:NotBlank(message = "Character name is required")
    val characterName: String,

    @field:NotBlank(message = "Reason is required")
    val reason: String,

    @field:NotBlank(message = "Start date is required")
    val startDate: String, // ISO date string

    val endDate: String? = null, // ISO date string
)

/**
 * Request DTO for updating a loot ban via admin API.
 */
data class AdminLootBanUpdateRequest(
    val reason: String? = null,
    val endDate: String? = null, // ISO date string or null to clear
    val active: Boolean? = null,
)

/**
 * Response DTO for loot ban via admin API.
 * Maps to frontend LootBan type.
 */
data class AdminLootBanResponse(
    val id: Long,
    val raiderId: Long,
    val characterName: String,
    val reason: String,
    val startDate: String, // ISO date string
    val endDate: String?, // ISO date string
    val createdBy: String,
    val active: Boolean,
) {
    companion object {
        fun from(response: LootBanResponse): AdminLootBanResponse {
            return AdminLootBanResponse(
                id = response.id,
                raiderId = 0L, // Backend doesn't track raiderId for loot bans
                characterName = response.characterName,
                reason = response.reason,
                startDate = response.bannedAt.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                endDate = response.expiresAt?.atOffset(ZoneOffset.UTC)?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                createdBy = response.bannedBy,
                active = response.isActive,
            )
        }
    }
}
