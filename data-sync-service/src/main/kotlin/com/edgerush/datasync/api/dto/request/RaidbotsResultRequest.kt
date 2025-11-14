package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.Instant

data class CreateRaidbotsResultRequest(
    @field:Min(value = 0, message = "Simulation id must be positive")
    val simulationId: Long? = null,
    @field:Min(value = 0, message = "Item id must be positive")
    val itemId: Long? = null,
    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 255, message = "Item name must not exceed 255 characters")
    val itemName: String? = null,
    @field:NotBlank(message = "Slot is required")
    @field:Size(max = 255, message = "Slot must not exceed 255 characters")
    val slot: String? = null,
    val dpsGain: Double? = null,
    val percentGain: Double? = null,
    val calculatedAt: Instant? = null,
)

data class UpdateRaidbotsResultRequest(
    @field:Min(value = 0, message = "Simulation id must be positive")
    val simulationId: Long? = null,
    @field:Min(value = 0, message = "Item id must be positive")
    val itemId: Long? = null,
    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 255, message = "Item name must not exceed 255 characters")
    val itemName: String? = null,
    @field:NotBlank(message = "Slot is required")
    @field:Size(max = 255, message = "Slot must not exceed 255 characters")
    val slot: String? = null,
    val dpsGain: Double? = null,
    val percentGain: Double? = null,
    val calculatedAt: Instant? = null,
)
