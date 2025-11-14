package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateBehavioralActionRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    @field:NotBlank(message = "Action type is required")
    @field:Size(max = 255, message = "Action type must not exceed 255 characters")
    val actionType: String? = null,

    val deductionAmount: Double? = null
)

data class UpdateBehavioralActionRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    @field:NotBlank(message = "Action type is required")
    @field:Size(max = 255, message = "Action type must not exceed 255 characters")
    val actionType: String? = null,

    val deductionAmount: Double? = null
)
