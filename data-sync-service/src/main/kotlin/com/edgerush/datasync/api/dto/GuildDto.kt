package com.edgerush.datasync.api.dto

import com.edgerush.datasync.domain.shared.model.Guild
import java.time.OffsetDateTime

/**
 * DTO for Guild configuration responses
 */
data class GuildDto(
    val guildId: String,
    val name: String,
    val description: String?,
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: String?,
    val lastSyncError: String?,
    val timezone: String,
    val isActive: Boolean,
    val benchmarkMode: String,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
    val benchmarkUpdatedAt: OffsetDateTime?
) {
    companion object {
        fun from(guild: Guild): GuildDto {
            return GuildDto(
                guildId = guild.id.value,
                name = guild.name,
                description = guild.description,
                wowauditGuildUri = guild.wowauditGuildUri,
                wowauditBaseUrl = guild.wowauditBaseUrl,
                syncEnabled = guild.syncEnabled,
                syncCronExpression = guild.syncCronExpression,
                lastSyncAt = guild.lastSyncAt,
                lastSyncStatus = guild.lastSyncStatus?.name,
                lastSyncError = guild.lastSyncError,
                timezone = guild.timezone,
                isActive = guild.isActive,
                benchmarkMode = guild.benchmarkMode.name,
                customBenchmarkRms = guild.customBenchmarkRms,
                customBenchmarkIpi = guild.customBenchmarkIpi,
                benchmarkUpdatedAt = guild.benchmarkUpdatedAt
            )
        }
    }
}

/**
 * Request to update guild benchmark configuration
 */
data class UpdateBenchmarkRequest(
    val mode: String,
    val customRms: Double? = null,
    val customIpi: Double? = null
)

/**
 * Request to update WoWAudit configuration
 */
data class UpdateWoWAuditConfigRequest(
    val apiKey: String?,
    val guildUri: String?
)

/**
 * Request to enable/disable sync
 */
data class SetSyncEnabledRequest(
    val enabled: Boolean
)
