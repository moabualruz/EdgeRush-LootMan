package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateWarcraftLogsFightRequest(
    @field:Min(value = 0, message = "Report id must be positive")
    val reportId: Long? = null,

    @field:Min(value = 0, message = "Fight id must be positive")
    val fightId: Int? = null,

    @field:Min(value = 0, message = "Encounter id must be positive")
    val encounterId: Int? = null,

    @field:NotBlank(message = "Encounter name is required")
    @field:Size(max = 255, message = "Encounter name must not exceed 255 characters")
    val encounterName: String? = null,

    @field:NotBlank(message = "Difficulty is required")
    @field:Size(max = 255, message = "Difficulty must not exceed 255 characters")
    val difficulty: String? = null,

    val kill: Boolean? = null,

    @field:Min(value = 0, message = "Start time must be positive")
    val startTime: Long? = null,

    @field:Min(value = 0, message = "End time must be positive")
    val endTime: Long? = null,

    val bossPercentage: Double? = null
)

data class UpdateWarcraftLogsFightRequest(
    @field:Min(value = 0, message = "Report id must be positive")
    val reportId: Long? = null,

    @field:Min(value = 0, message = "Fight id must be positive")
    val fightId: Int? = null,

    @field:Min(value = 0, message = "Encounter id must be positive")
    val encounterId: Int? = null,

    @field:NotBlank(message = "Encounter name is required")
    @field:Size(max = 255, message = "Encounter name must not exceed 255 characters")
    val encounterName: String? = null,

    @field:NotBlank(message = "Difficulty is required")
    @field:Size(max = 255, message = "Difficulty must not exceed 255 characters")
    val difficulty: String? = null,

    val kill: Boolean? = null,

    @field:Min(value = 0, message = "Start time must be positive")
    val startTime: Long? = null,

    @field:Min(value = 0, message = "End time must be positive")
    val endTime: Long? = null,

    val bossPercentage: Double? = null
)
