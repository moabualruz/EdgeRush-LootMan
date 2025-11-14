package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateLootAwardRequest(
    @field:Min(value = 0, message = "Raider id must be positive")
    val raiderId: Long? = null,
    @field:Min(value = 0, message = "Item id must be positive")
    val itemId: Long? = null,
    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 255, message = "Item name must not exceed 255 characters")
    val itemName: String? = null,
    @field:NotBlank(message = "Tier is required")
    @field:Size(max = 255, message = "Tier must not exceed 255 characters")
    val tier: String? = null,
    val flps: Double? = null,
    val rdf: Double? = null,
    val awardedAt: java.time.OffsetDateTime? = null,
    val rclootcouncilId: String? = null,
    val icon: String? = null,
    val slot: String? = null,
    val quality: String? = null,
    val responseTypeId: Int? = null,
    val responseTypeName: String? = null,
    val responseTypeRgba: String? = null,
    val responseTypeExcluded: Boolean? = null,
    val propagatedResponseTypeId: Int? = null,
    val propagatedResponseTypeName: String? = null,
    val propagatedResponseTypeRgba: String? = null,
    val propagatedResponseTypeExcluded: Boolean? = null,
    val sameResponseAmount: Int? = null,
    val note: String? = null,
    val wishValue: Int? = null,
    val difficulty: String? = null,
    val discarded: Boolean? = null,
    val characterId: Long? = null,
    val awardedByCharacterId: Long? = null,
    val awardedByName: String? = null,
)

data class UpdateLootAwardRequest(
    @field:Min(value = 0, message = "Raider id must be positive")
    val raiderId: Long? = null,
    @field:Min(value = 0, message = "Item id must be positive")
    val itemId: Long? = null,
    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 255, message = "Item name must not exceed 255 characters")
    val itemName: String? = null,
    @field:NotBlank(message = "Tier is required")
    @field:Size(max = 255, message = "Tier must not exceed 255 characters")
    val tier: String? = null,
    val flps: Double? = null,
    val rdf: Double? = null,
    val awardedAt: java.time.OffsetDateTime? = null,
    val rclootcouncilId: String? = null,
    val icon: String? = null,
    val slot: String? = null,
    val quality: String? = null,
    val responseTypeId: Int? = null,
    val responseTypeName: String? = null,
    val responseTypeRgba: String? = null,
    val responseTypeExcluded: Boolean? = null,
    val propagatedResponseTypeId: Int? = null,
    val propagatedResponseTypeName: String? = null,
    val propagatedResponseTypeRgba: String? = null,
    val propagatedResponseTypeExcluded: Boolean? = null,
    val sameResponseAmount: Int? = null,
    val note: String? = null,
    val wishValue: Int? = null,
    val difficulty: String? = null,
    val discarded: Boolean? = null,
    val characterId: Long? = null,
    val awardedByCharacterId: Long? = null,
    val awardedByName: String? = null,
)
