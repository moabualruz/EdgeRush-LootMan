package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("guild_configurations")
data class GuildConfigurationEntity(
    @Id
    val id: Long? = null,
    val guildId: String,
    val guildName: String,
    val guildDescription: String?,
    val wowauditApiKeyEncrypted: String?,
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String = "https://wowaudit.com",
    val syncEnabled: Boolean = true,
    val syncCronExpression: String = "0 0 4 * * *",
    val syncRunOnStartup: Boolean = false,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: String?,
    val lastSyncError: String?,
    val timezone: String = "UTC",
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    val benchmarkMode: String = "THEORETICAL",
    val customBenchmarkRms: BigDecimal?,
    val customBenchmarkIpi: BigDecimal?,
    val benchmarkUpdatedAt: OffsetDateTime?,
)
