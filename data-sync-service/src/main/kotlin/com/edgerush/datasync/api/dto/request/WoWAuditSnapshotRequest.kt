package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateWoWAuditSnapshotRequest(
    @field:NotBlank(message = "Endpoint is required")
    @field:Size(max = 255, message = "Endpoint must not exceed 255 characters")
    val endpoint: String? = null,
    @field:NotBlank(message = "Raw payload is required")
    @field:Size(max = 255, message = "Raw payload must not exceed 255 characters")
    val rawPayload: String? = null,
    val syncedAt: OffsetDateTime? = null,
)

data class UpdateWoWAuditSnapshotRequest(
    @field:NotBlank(message = "Endpoint is required")
    @field:Size(max = 255, message = "Endpoint must not exceed 255 characters")
    val endpoint: String? = null,
    @field:NotBlank(message = "Raw payload is required")
    @field:Size(max = 255, message = "Raw payload must not exceed 255 characters")
    val rawPayload: String? = null,
    val syncedAt: OffsetDateTime? = null,
)
