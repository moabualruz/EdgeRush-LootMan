package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateWishlistSnapshotRequest(
    val raiderId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
    @field:NotBlank(message = "Raw payload is required")
    @field:Size(max = 255, message = "Raw payload must not exceed 255 characters")
    val rawPayload: String? = null,
    val syncedAt: OffsetDateTime? = null,
)

data class UpdateWishlistSnapshotRequest(
    val raiderId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
    @field:NotBlank(message = "Raw payload is required")
    @field:Size(max = 255, message = "Raw payload must not exceed 255 characters")
    val rawPayload: String? = null,
    val syncedAt: OffsetDateTime? = null,
)
