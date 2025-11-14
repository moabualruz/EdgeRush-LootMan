package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateSyncRunRequest(
    @field:NotBlank(message = "Source is required")
    @field:Size(max = 255, message = "Source must not exceed 255 characters")
    val source: String? = null,

    @field:NotBlank(message = "Status is required")
    @field:Size(max = 255, message = "Status must not exceed 255 characters")
    val status: String? = null,

    val startedAt: java.time.OffsetDateTime? = null,

    val completedAt: java.time.OffsetDateTime? = null,

    val message: String? = null
)

data class UpdateSyncRunRequest(
    @field:NotBlank(message = "Source is required")
    @field:Size(max = 255, message = "Source must not exceed 255 characters")
    val source: String? = null,

    @field:NotBlank(message = "Status is required")
    @field:Size(max = 255, message = "Status must not exceed 255 characters")
    val status: String? = null,

    val startedAt: java.time.OffsetDateTime? = null,

    val completedAt: java.time.OffsetDateTime? = null,

    val message: String? = null
)
