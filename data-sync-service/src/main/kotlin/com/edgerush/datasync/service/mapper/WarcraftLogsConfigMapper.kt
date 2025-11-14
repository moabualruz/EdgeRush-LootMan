package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsConfigRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsConfigRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsConfigResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsConfigEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WarcraftLogsConfigMapper {

    fun toEntity(request: CreateWarcraftLogsConfigRequest): WarcraftLogsConfigEntity {
        return WarcraftLogsConfigEntity(
            guildId = "", // TODO: Set from request or context
            enabled = request.enabled ?: false,
            guildName = request.guildName ?: "",
            realm = request.realm ?: "",
            region = request.region ?: "",
            encryptedClientId = request.encryptedClientId,
            encryptedClientSecret = request.encryptedClientSecret,
            configJson = request.configJson ?: "",
            updatedAt = Instant.now(),
            updatedBy = request.updatedBy,
        )
    }

    fun updateEntity(entity: WarcraftLogsConfigEntity, request: UpdateWarcraftLogsConfigRequest): WarcraftLogsConfigEntity {
        return entity.copy(
            enabled = request.enabled ?: entity.enabled,
            guildName = request.guildName ?: entity.guildName,
            realm = request.realm ?: entity.realm,
            region = request.region ?: entity.region,
            encryptedClientId = request.encryptedClientId ?: entity.encryptedClientId,
            encryptedClientSecret = request.encryptedClientSecret ?: entity.encryptedClientSecret,
            configJson = request.configJson ?: entity.configJson,
            updatedBy = request.updatedBy ?: entity.updatedBy,
            updatedAt = Instant.now()
        )
    }

    fun toResponse(entity: WarcraftLogsConfigEntity): WarcraftLogsConfigResponse {
        return WarcraftLogsConfigResponse(
            guildId = entity.guildId,
            enabled = entity.enabled,
            guildName = entity.guildName,
            realm = entity.realm,
            region = entity.region,
            encryptedClientId = entity.encryptedClientId!!,
            encryptedClientSecret = entity.encryptedClientSecret!!,
            configJson = entity.configJson,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy!!,
        )
    }
}
