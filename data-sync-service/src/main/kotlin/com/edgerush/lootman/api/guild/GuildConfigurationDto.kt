package com.edgerush.lootman.api.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Request DTO for creating a guild configuration.
 */
data class CreateGuildConfigurationRequest(
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    @field:NotBlank(message = "Guild name is required")
    val guildName: String,
    val guildDescription: String? = null,
    val wowauditApiKeyEncrypted: String? = null,
    val wowauditGuildUri: String? = null,
    val wowauditBaseUrl: String = "https://wowaudit.com",
    val syncEnabled: Boolean = true,
    val syncCronExpression: String = "0 0 4 * * *",
    val syncRunOnStartup: Boolean = false,
    val timezone: String = "UTC",
    val benchmarkMode: String = "THEORETICAL",
    val customBenchmarkRms: BigDecimal? = null,
    val customBenchmarkIpi: BigDecimal? = null,
)

/**
 * Request DTO for updating a guild configuration.
 */
data class UpdateGuildConfigurationRequest(
    val guildName: String? = null,
    val guildDescription: String? = null,
    val wowauditApiKeyEncrypted: String? = null,
    val wowauditGuildUri: String? = null,
    val wowauditBaseUrl: String? = null,
    val syncEnabled: Boolean? = null,
    val syncCronExpression: String? = null,
    val syncRunOnStartup: Boolean? = null,
    val timezone: String? = null,
    val isActive: Boolean? = null,
)

/**
 * Request DTO for updating benchmark configuration.
 */
data class UpdateBenchmarkRequest(
    val benchmarkMode: String? = null,
    val customBenchmarkRms: BigDecimal? = null,
    val customBenchmarkIpi: BigDecimal? = null,
)

/**
 * Response DTO for a guild configuration.
 */
data class GuildConfigurationResponse(
    val id: Long,
    val guildId: String,
    val guildName: String,
    val guildDescription: String?,
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val syncRunOnStartup: Boolean,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: String?,
    val lastSyncError: String?,
    val timezone: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val benchmarkMode: String,
    val customBenchmarkRms: BigDecimal?,
    val customBenchmarkIpi: BigDecimal?,
    val benchmarkUpdatedAt: OffsetDateTime?,
) {
    companion object {
        fun from(entity: GuildConfigurationEntity): GuildConfigurationResponse =
            GuildConfigurationResponse(
                id = entity.id ?: 0L,
                guildId = entity.guildId,
                guildName = entity.guildName,
                guildDescription = entity.guildDescription,
                wowauditGuildUri = entity.wowauditGuildUri,
                wowauditBaseUrl = entity.wowauditBaseUrl,
                syncEnabled = entity.syncEnabled,
                syncCronExpression = entity.syncCronExpression,
                syncRunOnStartup = entity.syncRunOnStartup,
                lastSyncAt = entity.lastSyncAt,
                lastSyncStatus = entity.lastSyncStatus,
                lastSyncError = entity.lastSyncError,
                timezone = entity.timezone,
                isActive = entity.isActive,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                benchmarkMode = entity.benchmarkMode,
                customBenchmarkRms = entity.customBenchmarkRms,
                customBenchmarkIpi = entity.customBenchmarkIpi,
                benchmarkUpdatedAt = entity.benchmarkUpdatedAt,
            )
    }
}

/**
 * Response DTO for checking if a guild configuration exists.
 */
data class GuildConfigurationExistsResponse(
    val exists: Boolean,
)
