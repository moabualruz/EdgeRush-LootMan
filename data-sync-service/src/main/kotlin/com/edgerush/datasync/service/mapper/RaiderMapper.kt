package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRequest
import com.edgerush.datasync.api.dto.response.RaiderResponse
import com.edgerush.datasync.entity.RaiderEntity
import org.springframework.stereotype.Component

@Component
class RaiderMapper {
    fun toEntity(request: CreateRaiderRequest): RaiderEntity {
        return RaiderEntity(
            id = null,
            characterName = request.characterName ?: "",
            realm = request.realm ?: "",
            region = request.region ?: "",
            wowauditId = 0L, // System populated
            clazz = "", // System populated
            spec = "", // System populated - non-null String
            role = "", // System populated - non-null String
            rank = null, // System populated
            status = null, // System populated
            note = null, // System populated
            blizzardId = null, // System populated
            trackingSince = java.time.OffsetDateTime.now(), // System populated - non-null
            joinDate = null, // System populated
            blizzardLastModified = null, // System populated
            lastSync = java.time.OffsetDateTime.now(), // System populated - non-null
        )
    }

    fun updateEntity(
        entity: RaiderEntity,
        request: UpdateRaiderRequest,
    ): RaiderEntity {
        return entity.copy(
            characterName = request.characterName ?: entity.characterName,
            realm = request.realm ?: entity.realm,
            region = request.region ?: entity.region,
        )
    }

    fun toResponse(entity: RaiderEntity): RaiderResponse {
        return RaiderResponse(
            id = entity.id!!,
            characterName = entity.characterName,
            realm = entity.realm,
            region = entity.region,
        )
    }
}
