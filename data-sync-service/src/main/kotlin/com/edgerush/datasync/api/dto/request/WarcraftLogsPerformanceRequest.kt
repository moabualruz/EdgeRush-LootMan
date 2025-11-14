package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.Instant

data class CreateWarcraftLogsPerformanceRequest(
    @field:Min(value = 0, message = "Fight id must be positive")
    val fightId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    @field:NotBlank(message = "Character class is required")
    @field:Size(max = 255, message = "Character class must not exceed 255 characters")
    val characterClass: String? = null,
    @field:NotBlank(message = "Character spec is required")
    @field:Size(max = 255, message = "Character spec must not exceed 255 characters")
    val characterSpec: String? = null,
    @field:Min(value = 0, message = "Deaths must be positive")
    val deaths: Int? = null,
    @field:Min(value = 0, message = "Damage taken must be positive")
    val damageTaken: Long? = null,
    @field:Min(value = 0, message = "Avoidable damage taken must be positive")
    val avoidableDamageTaken: Long? = null,
    val avoidableDamagePercentage: Double? = null,
    val performancePercentile: Double? = null,
    @field:Min(value = 0, message = "Item level must be positive")
    val itemLevel: Int? = null,
    val calculatedAt: Instant? = null,
)

data class UpdateWarcraftLogsPerformanceRequest(
    @field:Min(value = 0, message = "Fight id must be positive")
    val fightId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,
    @field:NotBlank(message = "Character realm is required")
    @field:Size(max = 255, message = "Character realm must not exceed 255 characters")
    val characterRealm: String? = null,
    @field:NotBlank(message = "Character class is required")
    @field:Size(max = 255, message = "Character class must not exceed 255 characters")
    val characterClass: String? = null,
    @field:NotBlank(message = "Character spec is required")
    @field:Size(max = 255, message = "Character spec must not exceed 255 characters")
    val characterSpec: String? = null,
    @field:Min(value = 0, message = "Deaths must be positive")
    val deaths: Int? = null,
    @field:Min(value = 0, message = "Damage taken must be positive")
    val damageTaken: Long? = null,
    @field:Min(value = 0, message = "Avoidable damage taken must be positive")
    val avoidableDamageTaken: Long? = null,
    val avoidableDamagePercentage: Double? = null,
    val performancePercentile: Double? = null,
    @field:Min(value = 0, message = "Item level must be positive")
    val itemLevel: Int? = null,
    val calculatedAt: Instant? = null,
)
