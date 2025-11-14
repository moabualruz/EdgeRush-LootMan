package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.Instant

data class CreateRaidbotsSimulationRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    @field:NotBlank(message = "Sim id is required")
    @field:Size(max = 255, message = "Sim id must not exceed 255 characters")
    val simId: String? = null,
    @field:NotBlank(message = "Status is required")
    @field:Size(max = 255, message = "Status must not exceed 255 characters")
    val status: String? = null,
    val submittedAt: Instant? = null,
    val completedAt: Instant? = null,
    @field:NotBlank(message = "Profile is required")
    @field:Size(max = 255, message = "Profile must not exceed 255 characters")
    val profile: String? = null,
    @field:NotBlank(message = "Sim options is required")
    @field:Size(max = 255, message = "Sim options must not exceed 255 characters")
    val simOptions: String? = null,
)

data class UpdateRaidbotsSimulationRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    @field:NotBlank(message = "Sim id is required")
    @field:Size(max = 255, message = "Sim id must not exceed 255 characters")
    val simId: String? = null,
    @field:NotBlank(message = "Status is required")
    @field:Size(max = 255, message = "Status must not exceed 255 characters")
    val status: String? = null,
    val submittedAt: Instant? = null,
    val completedAt: Instant? = null,
    @field:NotBlank(message = "Profile is required")
    @field:Size(max = 255, message = "Profile must not exceed 255 characters")
    val profile: String? = null,
    @field:NotBlank(message = "Sim options is required")
    @field:Size(max = 255, message = "Sim options must not exceed 255 characters")
    val simOptions: String? = null,
)
