package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateGuildConfigurationRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,

    @field:NotBlank(message = "Guild name is required")
    @field:Size(max = 255, message = "Guild name must not exceed 255 characters")
    val guildName: String? = null,

    val guildDescription: String? = null,

    val wowauditApiKeyEncrypted: String? = null,

    val wowauditGuildUri: String? = null,

    @field:NotBlank(message = "Wowaudit base url is required")
    @field:Size(max = 255, message = "Wowaudit base url must not exceed 255 characters")
    val wowauditBaseUrl: String? = null,

    val syncEnabled: Boolean? = null,

    @field:NotBlank(message = "Sync cron expression is required")
    @field:Size(max = 255, message = "Sync cron expression must not exceed 255 characters")
    val syncCronExpression: String? = null,

    val syncRunOnStartup: Boolean? = null,

    val lastSyncAt: OffsetDateTime? = null,

    val lastSyncStatus: String? = null,

    val lastSyncError: String? = null,

    @field:NotBlank(message = "Timezone is required")
    @field:Size(max = 255, message = "Timezone must not exceed 255 characters")
    val timezone: String? = null,

    val isActive: Boolean? = null
)

data class UpdateGuildConfigurationRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,

    @field:NotBlank(message = "Guild name is required")
    @field:Size(max = 255, message = "Guild name must not exceed 255 characters")
    val guildName: String? = null,

    val guildDescription: String? = null,

    val wowauditApiKeyEncrypted: String? = null,

    val wowauditGuildUri: String? = null,

    @field:NotBlank(message = "Wowaudit base url is required")
    @field:Size(max = 255, message = "Wowaudit base url must not exceed 255 characters")
    val wowauditBaseUrl: String? = null,

    val syncEnabled: Boolean? = null,

    @field:NotBlank(message = "Sync cron expression is required")
    @field:Size(max = 255, message = "Sync cron expression must not exceed 255 characters")
    val syncCronExpression: String? = null,

    val syncRunOnStartup: Boolean? = null,

    val lastSyncAt: OffsetDateTime? = null,

    val lastSyncStatus: String? = null,

    val lastSyncError: String? = null,

    @field:NotBlank(message = "Timezone is required")
    @field:Size(max = 255, message = "Timezone must not exceed 255 characters")
    val timezone: String? = null,

    val isActive: Boolean? = null
)
