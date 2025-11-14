package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class CreateRaidRequest(
    @field:NotNull(message = "Date is required")
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val instance: String? = null,
    val difficulty: String? = null,
    val optional: Boolean? = false,
    val status: String? = "Scheduled",
    val notes: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null
)

data class UpdateRaidRequest(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val instance: String? = null,
    val difficulty: String? = null,
    val optional: Boolean? = null,
    val status: String? = null,
    val notes: String? = null
)
