package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.request.UpdateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.response.GuildConfigurationResponse
import com.edgerush.datasync.entity.GuildConfigurationEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class GuildConfigurationMapper {

    fun toEntity(request: CreateGuildConfigurationRequest): GuildConfigurationEntity {
        return GuildConfigurationEntity(
            id = null,
            guildId = request.guildId ?: "",
            guildName = request.guildName ?: "",
            guildDescription = request.guildDescription,
            wowauditApiKeyEncrypted = request.wowauditApiKeyEncrypted,
            wowauditGuildUri = request.wowauditGuildUri,
            wowauditBaseUrl = request.wowauditBaseUrl ?: "",
            syncEnabled = request.syncEnabled ?: false,
            syncCronExpression = request.syncCronExpression ?: "",
            syncRunOnStartup = request.syncRunOnStartup ?: false,
            lastSyncAt = request.lastSyncAt,
            lastSyncStatus = request.lastSyncStatus,
            lastSyncError = request.lastSyncError,
            timezone = request.timezone ?: "",
            isActive = request.isActive ?: false,
            createdAt = OffsetDateTime.now(),
            customBenchmarkRms = null, // System populated
            customBenchmarkIpi = null, // System populated
            benchmarkUpdatedAt = null // System populated
        )
    }

    fun updateEntity(entity: GuildConfigurationEntity, request: UpdateGuildConfigurationRequest): GuildConfigurationEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            guildName = request.guildName ?: entity.guildName,
            guildDescription = request.guildDescription ?: entity.guildDescription,
            wowauditApiKeyEncrypted = request.wowauditApiKeyEncrypted ?: entity.wowauditApiKeyEncrypted,
            wowauditGuildUri = request.wowauditGuildUri ?: entity.wowauditGuildUri,
            wowauditBaseUrl = request.wowauditBaseUrl ?: entity.wowauditBaseUrl,
            syncEnabled = request.syncEnabled ?: entity.syncEnabled,
            syncCronExpression = request.syncCronExpression ?: entity.syncCronExpression,
            syncRunOnStartup = request.syncRunOnStartup ?: entity.syncRunOnStartup,
            lastSyncAt = request.lastSyncAt ?: entity.lastSyncAt,
            lastSyncStatus = request.lastSyncStatus ?: entity.lastSyncStatus,
            lastSyncError = request.lastSyncError ?: entity.lastSyncError,
            timezone = request.timezone ?: entity.timezone,
            isActive = request.isActive ?: entity.isActive,
        )
    }

    fun toResponse(entity: GuildConfigurationEntity): GuildConfigurationResponse {
        return GuildConfigurationResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            guildName = entity.guildName,
            guildDescription = entity.guildDescription!!,
            wowauditApiKeyEncrypted = entity.wowauditApiKeyEncrypted!!,
            wowauditGuildUri = entity.wowauditGuildUri!!,
            wowauditBaseUrl = entity.wowauditBaseUrl,
            syncEnabled = entity.syncEnabled,
            syncCronExpression = entity.syncCronExpression,
            syncRunOnStartup = entity.syncRunOnStartup,
            lastSyncAt = entity.lastSyncAt!!,
            lastSyncStatus = entity.lastSyncStatus!!,
            lastSyncError = entity.lastSyncError!!,
            timezone = entity.timezone,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
        )
    }
}
