package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.response.RaidbotsSimulationResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsSimulationEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RaidbotsSimulationMapper {

    fun toEntity(request: CreateRaidbotsSimulationRequest): RaidbotsSimulationEntity {
        return RaidbotsSimulationEntity(
            id = null,
            guildId = request.guildId ?: "",
            characterName = request.characterName ?: "",
            characterRealm = request.characterRealm ?: "",
            simId = request.simId ?: "",
            status = request.status ?: "",
            submittedAt = request.submittedAt ?: Instant.now(),
            completedAt = request.completedAt,
            profile = request.profile ?: "",
            simOptions = request.simOptions ?: "",
        )
    }

    fun updateEntity(entity: RaidbotsSimulationEntity, request: UpdateRaidbotsSimulationRequest): RaidbotsSimulationEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            simId = request.simId ?: entity.simId,
            status = request.status ?: entity.status,
            submittedAt = request.submittedAt ?: entity.submittedAt,
            completedAt = request.completedAt ?: entity.completedAt,
            profile = request.profile ?: entity.profile,
            simOptions = request.simOptions ?: entity.simOptions,
        )
    }

    fun toResponse(entity: RaidbotsSimulationEntity): RaidbotsSimulationResponse {
        return RaidbotsSimulationResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm,
            simId = entity.simId,
            status = entity.status,
            submittedAt = entity.submittedAt,
            completedAt = entity.completedAt!!,
            profile = entity.profile,
            simOptions = entity.simOptions,
        )
    }
}
