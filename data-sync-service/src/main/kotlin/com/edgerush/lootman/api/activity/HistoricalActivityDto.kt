package com.edgerush.lootman.api.activity

import com.edgerush.datasync.entity.HistoricalActivityEntity
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateHistoricalActivityRequest(
    val characterId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    val characterRealm: String? = null,
    val periodId: Long? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    @field:NotBlank(message = "Data JSON is required")
    val dataJson: String,
)

data class UpdateHistoricalActivityRequest(
    val dataJson: String? = null,
)

data class HistoricalActivityResponse(
    val id: Long,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val periodId: Long?,
    val teamId: Long?,
    val seasonId: Long?,
    val dataJson: String,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(e: HistoricalActivityEntity) =
            HistoricalActivityResponse(
                e.id!!, e.characterId, e.characterName, e.characterRealm,
                e.periodId, e.teamId, e.seasonId, e.dataJson, e.syncedAt,
            )
    }
}

data class HistoricalActivityExistsResponse(val exists: Boolean)

data class HistoricalActivityCountResponse(val count: Long)
