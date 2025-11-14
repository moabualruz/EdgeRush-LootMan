package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.OffsetDateTime

data class CreateAttendanceStatRequest(
    val instance: String? = null,

    val encounter: String? = null,

    val startDate: LocalDate? = null,

    val endDate: LocalDate? = null,

    val characterId: Long? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    val characterRealm: String? = null,

    val characterClass: String? = null,

    val characterRole: String? = null,

    val characterRegion: String? = null,

    val attendedAmountOfRaids: Int? = null,

    val totalAmountOfRaids: Int? = null,

    val attendedPercentage: Double? = null,

    val selectedAmountOfEncounters: Int? = null,

    val totalAmountOfEncounters: Int? = null,

    val selectedPercentage: Double? = null,

    val teamId: Long? = null,

    val seasonId: Long? = null,

    val periodId: Long? = null,

    val syncedAt: OffsetDateTime? = null
)

data class UpdateAttendanceStatRequest(
    val instance: String? = null,

    val encounter: String? = null,

    val startDate: LocalDate? = null,

    val endDate: LocalDate? = null,

    val characterId: Long? = null,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 255, message = "Character name must not exceed 255 characters")
    val characterName: String? = null,

    val characterRealm: String? = null,

    val characterClass: String? = null,

    val characterRole: String? = null,

    val characterRegion: String? = null,

    val attendedAmountOfRaids: Int? = null,

    val totalAmountOfRaids: Int? = null,

    val attendedPercentage: Double? = null,

    val selectedAmountOfEncounters: Int? = null,

    val totalAmountOfEncounters: Int? = null,

    val selectedPercentage: Double? = null,

    val teamId: Long? = null,

    val seasonId: Long? = null,

    val periodId: Long? = null,

    val syncedAt: OffsetDateTime? = null
)
