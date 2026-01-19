package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("guild_configurations")
data class GuildConfigurationEntity(
    @Id
    val id: Long? = null,
    @Column("guild_id")
    val guildId: String,
    @Column("guild_name")
    val guildName: String,
    @Column("guild_description")
    val guildDescription: String?,
    @Column("wowaudit_api_key_encrypted")
    val wowauditApiKeyEncrypted: String?,
    @Column("wowaudit_guild_uri")
    val wowauditGuildUri: String?,
    @Column("wowaudit_base_url")
    val wowauditBaseUrl: String = "https://wowaudit.com",
    @Column("sync_enabled")
    val syncEnabled: Boolean = true,
    @Column("sync_cron_expression")
    val syncCronExpression: String = "0 0 4 * * *",
    @Column("sync_run_on_startup")
    val syncRunOnStartup: Boolean = false,
    @Column("last_sync_at")
    val lastSyncAt: OffsetDateTime?,
    @Column("last_sync_status")
    val lastSyncStatus: String?,
    @Column("last_sync_error")
    val lastSyncError: String?,
    @Column("timezone")
    val timezone: String = "UTC",
    @Column("is_active")
    val isActive: Boolean = true,
    @Column("created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column("updated_at")
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    @Column("benchmark_mode")
    val benchmarkMode: String = "THEORETICAL",
    @Column("custom_benchmark_rms")
    val customBenchmarkRms: BigDecimal?,
    @Column("custom_benchmark_ipi")
    val customBenchmarkIpi: BigDecimal?,
    @Column("benchmark_updated_at")
    val benchmarkUpdatedAt: OffsetDateTime?,
    // Battle.net guild roster config
    @Column("bnet_realm_slug")
    val bnetRealmSlug: String? = null,
    @Column("bnet_guild_name_slug")
    val bnetGuildNameSlug: String? = null,
    @Column("bnet_region")
    val bnetRegion: String = "eu",
    @Column("bnet_last_sync_at")
    val bnetLastSyncAt: OffsetDateTime? = null,
    @Column("bnet_last_sync_status")
    val bnetLastSyncStatus: String? = null,
    @Column("bnet_last_sync_error")
    val bnetLastSyncError: String? = null,
    @Column("bnet_sync_enabled")
    val bnetSyncEnabled: Boolean = true,
)
