package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class GuildConfigurationResponse(
    val id: Long?,
    val guildId: String,
    val guildName: String,
    val guildDescription: String?,
    val wowauditApiKeyEncrypted: String?,
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
)
