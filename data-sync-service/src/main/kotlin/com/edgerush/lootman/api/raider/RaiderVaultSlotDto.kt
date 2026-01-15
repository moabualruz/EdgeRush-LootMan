package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * Request DTO for creating a raider vault slot.
 */
data class CreateRaiderVaultSlotRequest(
    @field:NotNull(message = "Raider ID is required")
    @field:Positive(message = "Raider ID must be positive")
    val raiderId: Long,
    @field:NotBlank(message = "Slot is required")
    val slot: String,
    val unlocked: Boolean? = false,
)

/**
 * Request DTO for updating a raider vault slot.
 */
data class UpdateRaiderVaultSlotRequest(
    val slot: String? = null,
    val unlocked: Boolean? = null,
)

/**
 * Response DTO for a raider vault slot.
 */
data class RaiderVaultSlotResponse(
    val id: Long,
    val raiderId: Long,
    val slot: String,
    val unlocked: Boolean?,
) {
    companion object {
        fun from(entity: RaiderVaultSlotEntity): RaiderVaultSlotResponse =
            RaiderVaultSlotResponse(
                id = entity.id ?: 0L,
                raiderId = entity.raiderId,
                slot = entity.slot,
                unlocked = entity.unlocked,
            )
    }
}

/**
 * Response DTO for checking if a vault slot exists.
 */
data class RaiderVaultSlotExistsResponse(
    val exists: Boolean,
)

/**
 * Response DTO for vault slot count.
 */
data class RaiderVaultSlotCountResponse(
    val count: Long,
)
