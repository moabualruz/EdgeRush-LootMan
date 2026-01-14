package com.edgerush.lootman.api.team

import com.edgerush.datasync.entity.TeamRaidDayEntity
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Request DTO for creating a team raid day.
 */
data class CreateTeamRaidDayRequest(
    @field:NotNull(message = "Team ID is required")
    val teamId: Long,
    val weekDay: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val currentInstance: String? = null,
    val difficulty: String? = null,
    val activeFrom: LocalDate? = null,
)

/**
 * Request DTO for updating a team raid day.
 */
data class UpdateTeamRaidDayRequest(
    val weekDay: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val currentInstance: String? = null,
    val difficulty: String? = null,
    val activeFrom: LocalDate? = null,
)

/**
 * Response DTO for team raid day.
 */
data class TeamRaidDayResponse(
    val id: Long,
    val teamId: Long,
    val weekDay: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val currentInstance: String?,
    val difficulty: String?,
    val activeFrom: LocalDate?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: TeamRaidDayEntity): TeamRaidDayResponse =
            TeamRaidDayResponse(
                id = entity.id!!,
                teamId = entity.teamId,
                weekDay = entity.weekDay,
                startTime = entity.startTime,
                endTime = entity.endTime,
                currentInstance = entity.currentInstance,
                difficulty = entity.difficulty,
                activeFrom = entity.activeFrom,
                syncedAt = entity.syncedAt,
            )
    }
}

/**
 * Response DTO for exists check.
 */
data class TeamRaidDayExistsResponse(val exists: Boolean)

/**
 * Response DTO for count.
 */
data class TeamRaidDayCountResponse(val count: Long)
