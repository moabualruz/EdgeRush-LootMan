package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEncounterEntity
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * Request DTO for creating a raid encounter.
 */
data class CreateRaidEncounterRequest(
    @field:NotNull(message = "Raid ID is required")
    @field:Positive(message = "Raid ID must be positive")
    val raidId: Long,

    val encounterId: Long? = null,
    val name: String? = null,
    val enabled: Boolean? = true,
    val extra: Boolean? = false,
    val notes: String? = null,
)

/**
 * Request DTO for updating a raid encounter.
 */
data class UpdateRaidEncounterRequest(
    val encounterId: Long? = null,
    val name: String? = null,
    val enabled: Boolean? = null,
    val extra: Boolean? = null,
    val notes: String? = null,
)

/**
 * Response DTO for a raid encounter.
 */
data class RaidEncounterResponse(
    val id: Long,
    val raidId: Long,
    val encounterId: Long?,
    val name: String?,
    val enabled: Boolean?,
    val extra: Boolean?,
    val notes: String?,
) {
    companion object {
        fun from(entity: RaidEncounterEntity): RaidEncounterResponse = RaidEncounterResponse(
            id = entity.id ?: 0L,
            raidId = entity.raidId,
            encounterId = entity.encounterId,
            name = entity.name,
            enabled = entity.enabled,
            extra = entity.extra,
            notes = entity.notes,
        )
    }
}
