package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsCharacterMappingRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsCharacterMappingRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsCharacterMappingResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsCharacterMappingEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WarcraftLogsCharacterMappingMapper {
    fun toEntity(request: CreateWarcraftLogsCharacterMappingRequest): WarcraftLogsCharacterMappingEntity {
        return WarcraftLogsCharacterMappingEntity(
            id = null,
            guildId = request.guildId ?: "",
            wowauditName = request.wowauditName ?: "",
            wowauditRealm = request.wowauditRealm ?: "",
            warcraftLogsName = request.warcraftLogsName ?: "",
            warcraftLogsRealm = request.warcraftLogsRealm ?: "",
            createdAt = Instant.now(),
            createdBy = request.createdBy,
        )
    }

    fun updateEntity(
        entity: WarcraftLogsCharacterMappingEntity,
        request: UpdateWarcraftLogsCharacterMappingRequest,
    ): WarcraftLogsCharacterMappingEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            wowauditName = request.wowauditName ?: entity.wowauditName,
            wowauditRealm = request.wowauditRealm ?: entity.wowauditRealm,
            warcraftLogsName = request.warcraftLogsName ?: entity.warcraftLogsName,
            warcraftLogsRealm = request.warcraftLogsRealm ?: entity.warcraftLogsRealm,
            createdBy = request.createdBy ?: entity.createdBy,
        )
    }

    fun toResponse(entity: WarcraftLogsCharacterMappingEntity): WarcraftLogsCharacterMappingResponse {
        return WarcraftLogsCharacterMappingResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            wowauditName = entity.wowauditName,
            wowauditRealm = entity.wowauditRealm,
            warcraftLogsName = entity.warcraftLogsName,
            warcraftLogsRealm = entity.warcraftLogsRealm,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy!!,
        )
    }
}
