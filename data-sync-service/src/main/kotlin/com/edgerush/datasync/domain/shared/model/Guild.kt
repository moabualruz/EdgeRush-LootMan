package com.edgerush.datasync.domain.shared.model

import java.time.OffsetDateTime

/**
 * Guild aggregate root representing a WoW guild configuration.
 * 
 * This entity manages guild-level settings, sync configuration,
 * and benchmark settings for FLPS calculations.
 */
data class Guild(
    val id: GuildId,
    val name: String,
    val description: String?,
    val wowauditApiKeyEncrypted: String?,
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val syncRunOnStartup: Boolean,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: SyncStatus?,
    val lastSyncError: String?,
    val timezone: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val benchmarkMode: BenchmarkMode,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
    val benchmarkUpdatedAt: OffsetDateTime?
) {
    /**
     * Check if WoWAudit integration is configured
     */
    fun hasWoWAuditConfig(): Boolean {
        return !wowauditApiKeyEncrypted.isNullOrBlank() && !wowauditGuildUri.isNullOrBlank()
    }
    
    /**
     * Check if sync is enabled and configured
     */
    fun canSync(): Boolean {
        return syncEnabled && hasWoWAuditConfig()
    }
    
    /**
     * Update sync status after a sync operation
     */
    fun updateSyncStatus(
        status: SyncStatus,
        error: String? = null
    ): Guild {
        return copy(
            lastSyncAt = OffsetDateTime.now(),
            lastSyncStatus = status,
            lastSyncError = error,
            updatedAt = OffsetDateTime.now()
        )
    }
    
    /**
     * Update benchmark configuration
     */
    fun updateBenchmark(
        mode: BenchmarkMode,
        customRms: Double? = null,
        customIpi: Double? = null
    ): Guild {
        require(mode != BenchmarkMode.CUSTOM || (customRms != null && customIpi != null)) {
            "Custom benchmark mode requires RMS and IPI values"
        }
        
        return copy(
            benchmarkMode = mode,
            customBenchmarkRms = customRms,
            customBenchmarkIpi = customIpi,
            benchmarkUpdatedAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
    }
    
    /**
     * Update WoWAudit configuration
     */
    fun updateWoWAuditConfig(
        apiKey: String?,
        guildUri: String?
    ): Guild {
        return copy(
            wowauditApiKeyEncrypted = apiKey,
            wowauditGuildUri = guildUri,
            updatedAt = OffsetDateTime.now()
        )
    }
    
    /**
     * Enable or disable sync
     */
    fun setSyncEnabled(enabled: Boolean): Guild {
        return copy(
            syncEnabled = enabled,
            updatedAt = OffsetDateTime.now()
        )
    }
    
    /**
     * Deactivate the guild
     */
    fun deactivate(): Guild {
        return copy(
            isActive = false,
            updatedAt = OffsetDateTime.now()
        )
    }
    
    companion object {
        /**
         * Create a new guild with default settings
         */
        fun create(
            guildId: String,
            name: String,
            description: String? = null,
            wowauditApiKey: String? = null,
            wowauditGuildUri: String? = null
        ): Guild {
            require(guildId.isNotBlank()) { "Guild ID cannot be blank" }
            require(name.isNotBlank()) { "Guild name cannot be blank" }
            
            val now = OffsetDateTime.now()
            
            return Guild(
                id = GuildId(guildId),
                name = name,
                description = description,
                wowauditApiKeyEncrypted = wowauditApiKey,
                wowauditGuildUri = wowauditGuildUri,
                wowauditBaseUrl = "https://wowaudit.com",
                syncEnabled = true,
                syncCronExpression = "0 0 4 * * *", // 4 AM daily
                syncRunOnStartup = false,
                lastSyncAt = null,
                lastSyncStatus = null,
                lastSyncError = null,
                timezone = "UTC",
                isActive = true,
                createdAt = now,
                updatedAt = now,
                benchmarkMode = BenchmarkMode.THEORETICAL,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                benchmarkUpdatedAt = null
            )
        }
    }
}

/**
 * Benchmark mode for FLPS perfect score calculations
 */
enum class BenchmarkMode {
    /**
     * Use mathematical perfect scores (1.0 for all components)
     */
    THEORETICAL,
    
    /**
     * Use the highest actual scores achieved by guild members
     */
    TOP_PERFORMER,
    
    /**
     * Use manually set benchmark values
     */
    CUSTOM
}

/**
 * Sync operation status
 */
enum class SyncStatus {
    SUCCESS,
    FAILED,
    IN_PROGRESS,
    NEVER_RUN
}
