package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateWarcraftLogsConfigRequest(
    val enabled: Boolean? = null,
    @field:NotBlank(message = "Guild name is required")
    @field:Size(max = 255, message = "Guild name must not exceed 255 characters")
    val guildName: String? = null,
    @field:NotBlank(message = "Realm is required")
    @field:Size(max = 255, message = "Realm must not exceed 255 characters")
    val realm: String? = null,
    @field:NotBlank(message = "Region is required")
    @field:Size(max = 255, message = "Region must not exceed 255 characters")
    val region: String? = null,
    val encryptedClientId: String? = null,
    val encryptedClientSecret: String? = null,
    @field:NotBlank(message = "Config json is required")
    @field:Size(max = 255, message = "Config json must not exceed 255 characters")
    val configJson: String? = null,
    val updatedBy: String? = null,
)

data class UpdateWarcraftLogsConfigRequest(
    val enabled: Boolean? = null,
    @field:NotBlank(message = "Guild name is required")
    @field:Size(max = 255, message = "Guild name must not exceed 255 characters")
    val guildName: String? = null,
    @field:NotBlank(message = "Realm is required")
    @field:Size(max = 255, message = "Realm must not exceed 255 characters")
    val realm: String? = null,
    @field:NotBlank(message = "Region is required")
    @field:Size(max = 255, message = "Region must not exceed 255 characters")
    val region: String? = null,
    val encryptedClientId: String? = null,
    val encryptedClientSecret: String? = null,
    @field:NotBlank(message = "Config json is required")
    @field:Size(max = 255, message = "Config json must not exceed 255 characters")
    val configJson: String? = null,
    val updatedBy: String? = null,
)
