package com.edgerush.lootman.application.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

/**
 * Use case for tracking raid attendance.
 *
 * This use case orchestrates attendance tracking by:
 * 1. Validating the attendance data
 * 2. Creating an attendance record
 * 3. Persisting the record
 */
@Service
class TrackAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    /**
     * Executes the attendance tracking.
     *
     * @param command The tracking command with all required inputs
     * @return Result containing AttendanceTrackingResult or error
     */
    fun execute(command: TrackAttendanceCommand): Result<AttendanceTrackingResult> = runCatching {
        // Create attendance record (validation happens in domain model)
        val record = AttendanceRecord.create(
            raiderId = command.raiderId,
            guildId = command.guildId,
            instance = command.instance,
            encounter = command.encounter,
            startDate = command.startDate,
            endDate = command.endDate,
            attendedRaids = command.attendedRaids,
            totalRaids = command.totalRaids
        )

        // Persist the record
        val savedRecord = attendanceRepository.save(record)

        // Return result
        AttendanceTrackingResult(
            recordId = savedRecord.id,
            raiderId = savedRecord.raiderId,
            guildId = savedRecord.guildId,
            instance = savedRecord.instance,
            encounter = savedRecord.encounter,
            startDate = savedRecord.startDate,
            endDate = savedRecord.endDate,
            attendedRaids = savedRecord.attendedRaids,
            totalRaids = savedRecord.totalRaids,
            attendancePercentage = savedRecord.attendancePercentage,
            recordedAt = savedRecord.recordedAt
        )
    }
}

/**
 * Command for tracking attendance.
 */
data class TrackAttendanceCommand(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int
)

/**
 * Result of attendance tracking.
 */
data class AttendanceTrackingResult(
    val recordId: AttendanceRecordId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int,
    val attendancePercentage: Double,
    val recordedAt: Instant
)
