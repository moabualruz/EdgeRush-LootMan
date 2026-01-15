package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Request DTO for creating a raid.
 */
data class CreateRaidRequest(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    @field:NotBlank(message = "Instance is required")
    val instance: String? = null,
    val difficulty: String? = null,
    val optional: Boolean? = false,
    val status: String? = "SCHEDULED",
    @field:Positive(message = "Total size must be positive")
    val totalSize: Int? = null,
    val notes: String? = null,
    val selectionsImage: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
)

/**
 * Request DTO for updating a raid.
 */
data class UpdateRaidRequest(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val instance: String? = null,
    val difficulty: String? = null,
    val optional: Boolean? = null,
    val status: String? = null,
    val presentSize: Int? = null,
    val totalSize: Int? = null,
    val notes: String? = null,
    val selectionsImage: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
)

/**
 * Response DTO for a raid.
 */
data class RaidResponse(
    val raidId: Long,
    val date: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val instance: String?,
    val difficulty: String?,
    val optional: Boolean?,
    val status: String?,
    val presentSize: Int?,
    val totalSize: Int?,
    val notes: String?,
    val selectionsImage: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: RaidEntity): RaidResponse =
            RaidResponse(
                raidId = entity.raidId,
                date = entity.date,
                startTime = entity.startTime,
                endTime = entity.endTime,
                instance = entity.instance,
                difficulty = entity.difficulty,
                optional = entity.optional,
                status = entity.status,
                presentSize = entity.presentSize,
                totalSize = entity.totalSize,
                notes = entity.notes,
                selectionsImage = entity.selectionsImage,
                teamId = entity.teamId,
                seasonId = entity.seasonId,
                periodId = entity.periodId,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                syncedAt = entity.syncedAt,
            )
    }
}
