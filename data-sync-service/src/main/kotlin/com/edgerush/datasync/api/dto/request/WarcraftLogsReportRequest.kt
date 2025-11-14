package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.Instant

data class CreateWarcraftLogsReportRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Report code is required")
    @field:Size(max = 255, message = "Report code must not exceed 255 characters")
    val reportCode: String? = null,
    val title: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val owner: String? = null,
    val zone: Int? = null,
    val syncedAt: Instant? = null,
    val rawMetadata: String? = null,
)

data class UpdateWarcraftLogsReportRequest(
    @field:NotBlank(message = "Guild id is required")
    @field:Size(max = 255, message = "Guild id must not exceed 255 characters")
    val guildId: String? = null,
    @field:NotBlank(message = "Report code is required")
    @field:Size(max = 255, message = "Report code must not exceed 255 characters")
    val reportCode: String? = null,
    val title: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val owner: String? = null,
    val zone: Int? = null,
    val syncedAt: Instant? = null,
    val rawMetadata: String? = null,
)
