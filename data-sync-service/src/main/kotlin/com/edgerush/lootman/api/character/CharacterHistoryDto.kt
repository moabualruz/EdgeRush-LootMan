package com.edgerush.lootman.api.character

import com.edgerush.datasync.entity.CharacterHistoryEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

/**
 * Request DTO for creating character history.
 */
data class CreateCharacterHistoryRequest(
    @field:NotNull(message = "Character ID is required")
    val characterId: Long,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
    @field:NotBlank(message = "History JSON is required")
    val historyJson: String,
    val bestGearJson: String? = null,
)

/**
 * Request DTO for updating character history.
 */
data class UpdateCharacterHistoryRequest(
    val historyJson: String? = null,
    val bestGearJson: String? = null,
)

/**
 * Response DTO for character history.
 */
data class CharacterHistoryResponse(
    val id: Long,
    val characterId: Long,
    val characterName: String,
    val characterRealm: String?,
    val characterRegion: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val historyJson: String,
    val bestGearJson: String?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: CharacterHistoryEntity): CharacterHistoryResponse =
            CharacterHistoryResponse(
                id = entity.id!!,
                characterId = entity.characterId,
                characterName = entity.characterName,
                characterRealm = entity.characterRealm,
                characterRegion = entity.characterRegion,
                teamId = entity.teamId,
                seasonId = entity.seasonId,
                periodId = entity.periodId,
                historyJson = entity.historyJson,
                bestGearJson = entity.bestGearJson,
                syncedAt = entity.syncedAt,
            )
    }
}

/**
 * Response DTO for exists check.
 */
data class CharacterHistoryExistsResponse(val exists: Boolean)

/**
 * Response DTO for count.
 */
data class CharacterHistoryCountResponse(val count: Long)
