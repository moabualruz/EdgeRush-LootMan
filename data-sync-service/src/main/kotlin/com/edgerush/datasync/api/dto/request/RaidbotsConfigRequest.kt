package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateRaidbotsConfigRequest(
    val enabled: Boolean? = null,
    val encryptedApiKey: String? = null,
    @field:NotBlank(message = "Config json is required")
    @field:Size(max = 255, message = "Config json must not exceed 255 characters")
    val configJson: String? = null,
)

data class UpdateRaidbotsConfigRequest(
    val enabled: Boolean? = null,
    val encryptedApiKey: String? = null,
    @field:NotBlank(message = "Config json is required")
    @field:Size(max = 255, message = "Config json must not exceed 255 characters")
    val configJson: String? = null,
)
