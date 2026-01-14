package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidSignupEntity
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * Request DTO for creating a raid signup.
 */
data class CreateRaidSignupRequest(
    @field:NotNull(message = "Raid ID is required")
    @field:Positive(message = "Raid ID must be positive")
    val raidId: Long,

    val characterId: Long? = null,
    val characterName: String? = null,
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val characterClass: String? = null,
    val characterRole: String? = null,
    val characterGuest: Boolean? = false,
    val status: String? = "PENDING",
    val comment: String? = null,
    val selected: Boolean? = false,
)

/**
 * Request DTO for updating a raid signup.
 */
data class UpdateRaidSignupRequest(
    val characterId: Long? = null,
    val characterName: String? = null,
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val characterClass: String? = null,
    val characterRole: String? = null,
    val characterGuest: Boolean? = null,
    val status: String? = null,
    val comment: String? = null,
    val selected: Boolean? = null,
)

/**
 * Response DTO for a raid signup.
 */
data class RaidSignupResponse(
    val id: Long,
    val raidId: Long,
    val characterId: Long?,
    val characterName: String?,
    val characterRealm: String?,
    val characterRegion: String?,
    val characterClass: String?,
    val characterRole: String?,
    val characterGuest: Boolean?,
    val status: String?,
    val comment: String?,
    val selected: Boolean?,
) {
    companion object {
        fun from(entity: RaidSignupEntity): RaidSignupResponse = RaidSignupResponse(
            id = entity.id ?: 0L,
            raidId = entity.raidId,
            characterId = entity.characterId,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm,
            characterRegion = entity.characterRegion,
            characterClass = entity.characterClass,
            characterRole = entity.characterRole,
            characterGuest = entity.characterGuest,
            status = entity.status,
            comment = entity.comment,
            selected = entity.selected,
        )
    }
}
