package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.entity.AttendanceStatEntity
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Request DTO for creating an attendance stat.
 */
data class CreateAttendanceStatRequest(
    val instance: String? = null,
    val encounter: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val characterId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
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
)

/**
 * Request DTO for updating an attendance stat.
 */
data class UpdateAttendanceStatRequest(
    val instance: String? = null,
    val encounter: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val attendedAmountOfRaids: Int? = null,
    val totalAmountOfRaids: Int? = null,
    val attendedPercentage: Double? = null,
    val selectedAmountOfEncounters: Int? = null,
    val totalAmountOfEncounters: Int? = null,
    val selectedPercentage: Double? = null,
)

/**
 * Response DTO for attendance stat.
 */
data class AttendanceStatResponse(
    val id: Long,
    val instance: String?,
    val encounter: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val characterRegion: String?,
    val attendedAmountOfRaids: Int?,
    val totalAmountOfRaids: Int?,
    val attendedPercentage: Double?,
    val selectedAmountOfEncounters: Int?,
    val totalAmountOfEncounters: Int?,
    val selectedPercentage: Double?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: AttendanceStatEntity): AttendanceStatResponse =
            AttendanceStatResponse(
                id = entity.id!!,
                instance = entity.instance,
                encounter = entity.encounter,
                startDate = entity.startDate,
                endDate = entity.endDate,
                characterId = entity.characterId,
                characterName = entity.characterName,
                characterRealm = entity.characterRealm,
                characterClass = entity.characterClass,
                characterRole = entity.characterRole,
                characterRegion = entity.characterRegion,
                attendedAmountOfRaids = entity.attendedAmountOfRaids,
                totalAmountOfRaids = entity.totalAmountOfRaids,
                attendedPercentage = entity.attendedPercentage,
                selectedAmountOfEncounters = entity.selectedAmountOfEncounters,
                totalAmountOfEncounters = entity.totalAmountOfEncounters,
                selectedPercentage = entity.selectedPercentage,
                teamId = entity.teamId,
                seasonId = entity.seasonId,
                periodId = entity.periodId,
                syncedAt = entity.syncedAt,
            )
    }
}

/**
 * Response DTO for exists check.
 */
data class AttendanceStatExistsResponse(val exists: Boolean)

/**
 * Response DTO for count.
 */
data class AttendanceStatCountResponse(val count: Long)
