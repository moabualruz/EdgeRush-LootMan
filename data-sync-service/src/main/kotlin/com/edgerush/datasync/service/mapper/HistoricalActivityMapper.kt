package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.request.UpdateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.response.HistoricalActivityResponse
import com.edgerush.datasync.entity.HistoricalActivityEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class HistoricalActivityMapper {
    fun toEntity(request: CreateHistoricalActivityRequest): HistoricalActivityEntity {
        return HistoricalActivityEntity(
            id = null,
            characterId = request.characterId,
            characterName = request.characterName ?: "",
            characterRealm = request.characterRealm,
            periodId = request.periodId,
            teamId = request.teamId,
            seasonId = request.seasonId,
            dataJson = request.dataJson ?: "",
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: HistoricalActivityEntity,
        request: UpdateHistoricalActivityRequest,
    ): HistoricalActivityEntity {
        return entity.copy(
            characterId = request.characterId ?: entity.characterId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            periodId = request.periodId ?: entity.periodId,
            teamId = request.teamId ?: entity.teamId,
            seasonId = request.seasonId ?: entity.seasonId,
            dataJson = request.dataJson ?: entity.dataJson,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: HistoricalActivityEntity): HistoricalActivityResponse {
        return HistoricalActivityResponse(
            id = entity.id!!,
            characterId = entity.characterId!!,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm!!,
            periodId = entity.periodId!!,
            teamId = entity.teamId!!,
            seasonId = entity.seasonId!!,
            dataJson = entity.dataJson,
            syncedAt = entity.syncedAt,
        )
    }
}
