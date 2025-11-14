package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.request.UpdateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.response.CharacterHistoryResponse
import com.edgerush.datasync.entity.CharacterHistoryEntity
import org.springframework.stereotype.Component

@Component
class CharacterHistoryMapper {
    fun toEntity(request: CreateCharacterHistoryRequest): CharacterHistoryEntity {
        return CharacterHistoryEntity(
            id = null,
            characterId = 0L, // System populated
            characterName = "", // System populated
            characterRealm = null, // System populated
            characterRegion = null, // System populated
            teamId = null, // System populated
            seasonId = null, // System populated
            periodId = null, // System populated
            historyJson = "", // System populated
            bestGearJson = null, // System populated
            syncedAt = java.time.OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: CharacterHistoryEntity,
        request: UpdateCharacterHistoryRequest,
    ): CharacterHistoryEntity {
        return entity.copy()
    }

    fun toResponse(entity: CharacterHistoryEntity): CharacterHistoryResponse {
        return CharacterHistoryResponse(
            id = entity.id!!,
        )
    }
}
