package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateHistoricalActivityRequest(
    val characterId: Long? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    val characterRealm: String? = null,

    val periodId: Long? = null,

    val teamId: Long? = null,

    val seasonId: Long? = null,

    @field:NotBlank(message = "Data json is required")
    @field:Size(max = 255, message = "Data json must not exceed 255 characters")
    val dataJson: String? = null,

    val syncedAt: OffsetDateTime? = null
)

data class UpdateHistoricalActivityRequest(
    val characterId: Long? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    val characterRealm: String? = null,

    val periodId: Long? = null,

    val teamId: Long? = null,

    val seasonId: Long? = null,

    @field:NotBlank(message = "Data json is required")
    @field:Size(max = 255, message = "Data json must not exceed 255 characters")
    val dataJson: String? = null,

    val syncedAt: OffsetDateTime? = null
)
