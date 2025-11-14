package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.response.RaidbotsConfigResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsConfigEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RaidbotsConfigMapper {

    fun toEntity(request: CreateRaidbotsConfigRequest): RaidbotsConfigEntity {
        return RaidbotsConfigEntity(
            guildId = "", // TODO: Set from request or context
            enabled = request.enabled ?: false,
            encryptedApiKey = request.encryptedApiKey,
            configJson = request.configJson ?: "",
            updatedAt = Instant.now(),
        )
    }

    fun updateEntity(entity: RaidbotsConfigEntity, request: UpdateRaidbotsConfigRequest): RaidbotsConfigEntity {
        return entity.copy(
            enabled = request.enabled ?: entity.enabled,
            encryptedApiKey = request.encryptedApiKey ?: entity.encryptedApiKey,
            configJson = request.configJson ?: entity.configJson,
            updatedAt = Instant.now()
        )
    }

    fun toResponse(entity: RaidbotsConfigEntity): RaidbotsConfigResponse {
        return RaidbotsConfigResponse(
            guildId = entity.guildId,
            enabled = entity.enabled,
            encryptedApiKey = entity.encryptedApiKey!!,
            configJson = entity.configJson,
            updatedAt = entity.updatedAt,
        )
    }
}
