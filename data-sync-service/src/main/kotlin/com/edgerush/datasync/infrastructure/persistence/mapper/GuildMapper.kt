package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.shared.model.BenchmarkMode
import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.model.SyncStatus
import com.edgerush.datasync.entity.GuildConfigurationEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Guild domain model and GuildConfigurationEntity.
 */
@Component
class GuildMapper {
    /**
     * Convert domain model to entity
     */
    fun toEntity(guild: Guild): GuildConfigurationEntity {
        return GuildConfigurationEntity(
            id = null, // Let database generate ID
            guildId = guild.id.value,
            guildName = guild.name,
            guildDescription = guild.description,
            wowauditApiKeyEncrypted = guild.wowauditApiKeyEncrypted,
            wowauditGuildUri = guild.wowauditGuildUri,
            wowauditBaseUrl = guild.wowauditBaseUrl,
            syncEnabled = guild.syncEnabled,
            syncCronExpression = guild.syncCronExpression,
            syncRunOnStartup = guild.syncRunOnStartup,
            lastSyncAt = guild.lastSyncAt,
            lastSyncStatus = guild.lastSyncStatus?.name,
            lastSyncError = guild.lastSyncError,
            timezone = guild.timezone,
            isActive = guild.isActive,
            createdAt = guild.createdAt,
            updatedAt = guild.updatedAt,
            benchmarkMode = guild.benchmarkMode.name,
            customBenchmarkRms = guild.customBenchmarkRms?.toBigDecimal(),
            customBenchmarkIpi = guild.customBenchmarkIpi?.toBigDecimal(),
            benchmarkUpdatedAt = guild.benchmarkUpdatedAt
        )
    }
    
    /**
     * Convert entity to domain model
     */
    fun toDomain(entity: GuildConfigurationEntity): Guild {
        return Guild(
            id = GuildId(entity.guildId),
            name = entity.guildName,
            description = entity.guildDescription,
            wowauditApiKeyEncrypted = entity.wowauditApiKeyEncrypted,
            wowauditGuildUri = entity.wowauditGuildUri,
            wowauditBaseUrl = entity.wowauditBaseUrl,
            syncEnabled = entity.syncEnabled,
            syncCronExpression = entity.syncCronExpression,
            syncRunOnStartup = entity.syncRunOnStartup,
            lastSyncAt = entity.lastSyncAt,
            lastSyncStatus = entity.lastSyncStatus?.let { SyncStatus.valueOf(it) },
            lastSyncError = entity.lastSyncError,
            timezone = entity.timezone,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            benchmarkMode = BenchmarkMode.valueOf(entity.benchmarkMode),
            customBenchmarkRms = entity.customBenchmarkRms?.toDouble(),
            customBenchmarkIpi = entity.customBenchmarkIpi?.toDouble(),
            benchmarkUpdatedAt = entity.benchmarkUpdatedAt
        )
    }
}
