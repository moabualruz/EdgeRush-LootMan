package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateWarcraftLogsCharacterMappingRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Wowaudit name is required")
    @field:Size(max = 255, message = "Wowaudit name must not exceed 255 characters")
    val wowauditName: String? = null,
    @field:NotBlank(message = "Wowaudit realm is required")
    @field:Size(max = 255, message = "Wowaudit realm must not exceed 255 characters")
    val wowauditRealm: String? = null,
    @field:NotBlank(message = "Warcraft logs name is required")
    @field:Size(max = 255, message = "Warcraft logs name must not exceed 255 characters")
    val warcraftLogsName: String? = null,
    @field:NotBlank(message = "Warcraft logs realm is required")
    @field:Size(max = 255, message = "Warcraft logs realm must not exceed 255 characters")
    val warcraftLogsRealm: String? = null,
    val createdBy: String? = null,
)

data class UpdateWarcraftLogsCharacterMappingRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Wowaudit name is required")
    @field:Size(max = 255, message = "Wowaudit name must not exceed 255 characters")
    val wowauditName: String? = null,
    @field:NotBlank(message = "Wowaudit realm is required")
    @field:Size(max = 255, message = "Wowaudit realm must not exceed 255 characters")
    val wowauditRealm: String? = null,
    @field:NotBlank(message = "Warcraft logs name is required")
    @field:Size(max = 255, message = "Warcraft logs name must not exceed 255 characters")
    val warcraftLogsName: String? = null,
    @field:NotBlank(message = "Warcraft logs realm is required")
    @field:Size(max = 255, message = "Warcraft logs realm must not exceed 255 characters")
    val warcraftLogsRealm: String? = null,
    val createdBy: String? = null,
)
