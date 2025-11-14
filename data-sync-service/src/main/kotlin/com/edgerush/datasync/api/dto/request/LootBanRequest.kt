package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateLootBanRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Reason is required")
    @field:Size(max = 255, message = "Reason must not exceed 255 characters")
    val reason: String? = null,
    @field:NotBlank(message = "Banned by is required")
    @field:Size(max = 255, message = "Banned by must not exceed 255 characters")
    val bannedBy: String? = null,
    val bannedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
)

data class UpdateLootBanRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Reason is required")
    @field:Size(max = 255, message = "Reason must not exceed 255 characters")
    val reason: String? = null,
    @field:NotBlank(message = "Banned by is required")
    @field:Size(max = 255, message = "Banned by must not exceed 255 characters")
    val bannedBy: String? = null,
    val bannedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
)
