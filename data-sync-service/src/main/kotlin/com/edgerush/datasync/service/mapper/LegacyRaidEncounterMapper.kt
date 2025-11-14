package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidEncounterRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidEncounterRequest
import com.edgerush.datasync.api.dto.response.RaidEncounterResponse
import com.edgerush.datasync.entity.RaidEncounterEntity
import org.springframework.stereotype.Component

@Component
class LegacyRaidEncounterMapper {
    fun toEntity(request: CreateRaidEncounterRequest): RaidEncounterEntity {
        return RaidEncounterEntity(
            id = null,
            raidId = request.raidId ?: 0L,
            encounterId = request.encounterId,
            name = request.name,
            enabled = request.enabled,
            extra = request.extra,
            notes = request.notes,
        )
    }

    fun updateEntity(
        entity: RaidEncounterEntity,
        request: UpdateRaidEncounterRequest,
    ): RaidEncounterEntity {
        return entity.copy(
            raidId = request.raidId ?: entity.raidId,
            encounterId = request.encounterId ?: entity.encounterId,
            name = request.name ?: entity.name,
            enabled = request.enabled ?: entity.enabled,
            extra = request.extra ?: entity.extra,
            notes = request.notes ?: entity.notes,
        )
    }

    fun toResponse(entity: RaidEncounterEntity): RaidEncounterResponse {
        return RaidEncounterResponse(
            id = entity.id!!,
            raidId = entity.raidId,
            encounterId = entity.encounterId!!,
            name = entity.name!!,
            enabled = entity.enabled!!,
            extra = entity.extra!!,
            notes = entity.notes!!,
        )
    }
}
