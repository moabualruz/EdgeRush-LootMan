package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("guild_configurations")
data class GuildConfigurationEntity(
    @Id
    val id: Long? = null,
    val guildId: String,
    val guildName: String,
    val guildDescription: String?,
    
    // WoWAudit API Configuration
    val wowauditApiKeyEncrypted: String?,
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String = "https://wowaudit.com",
    
    // Sync Configuration  
    val syncEnabled: Boolean = true,
    val syncCronExpression: String = "0 0 4 * * *",
    val syncRunOnStartup: Boolean = false,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: String?,
    val lastSyncError: String?,
    
    // Guild Settings
    val timezone: String = "UTC",
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    
    // Perfect Score Benchmarking
    val benchmarkMode: String = "THEORETICAL", // THEORETICAL, TOP_PERFORMER, CUSTOM
    val customBenchmarkRms: java.math.BigDecimal?,
    val customBenchmarkIpi: java.math.BigDecimal?,
    val benchmarkUpdatedAt: OffsetDateTime?
)

enum class BenchmarkMode {
    THEORETICAL,    // Use mathematical perfect scores (1.0 for all components)
    TOP_PERFORMER, // Use the highest actual scores achieved by guild members
    CUSTOM         // Use manually set benchmark values
}

enum class SyncStatus {
    SUCCESS,
    FAILED, 
    IN_PROGRESS,
    NEVER_RUN
}