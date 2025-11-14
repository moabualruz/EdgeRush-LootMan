package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

data class CreateTeamRaidDayRequest(
    @field:Min(value = 0, message = "Team id must be positive")
    val teamId: Long? = null,

    val weekDay: String? = null,

    val startTime: LocalTime? = null,

    val endTime: LocalTime? = null,

    val currentInstance: String? = null,

    val difficulty: String? = null,

    val activeFrom: LocalDate? = null,

    val syncedAt: OffsetDateTime? = null
)

data class UpdateTeamRaidDayRequest(
    @field:Min(value = 0, message = "Team id must be positive")
    val teamId: Long? = null,

    val weekDay: String? = null,

    val startTime: LocalTime? = null,

    val endTime: LocalTime? = null,

    val currentInstance: String? = null,

    val difficulty: String? = null,

    val activeFrom: LocalDate? = null,

    val syncedAt: OffsetDateTime? = null
)
