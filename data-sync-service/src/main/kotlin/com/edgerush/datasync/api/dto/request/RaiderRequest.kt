package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateRaiderRequest(
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Realm is required")
    @field:Size(max = 255, message = "Realm must not exceed 255 characters")
    val realm: String? = null,
    @field:NotBlank(message = "Region is required")
    @field:Size(max = 255, message = "Region must not exceed 255 characters")
    val region: String? = null,
)

data class UpdateRaiderRequest(
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Realm is required")
    @field:Size(max = 255, message = "Realm must not exceed 255 characters")
    val realm: String? = null,
    @field:NotBlank(message = "Region is required")
    @field:Size(max = 255, message = "Region must not exceed 255 characters")
    val region: String? = null,
)
