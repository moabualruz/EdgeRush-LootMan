package com.edgerush.lootman.application.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Use case for tracking raid attendance.
 *
 * This use case orchestrates the process of:
 * 1. Validating the attendance data
 * 2. Creating an attendance record
 * 3. Persisting the record
 */
class TrackAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {

    /**
     * Execute the track attendance use case.
     *
     * @param command The command containing attendance tracking details
     * @return Result containing the created AttendanceRecord or an exception
     */
    fun execute(command: TrackAttendanceCommand): Result<AttendanceRecord> = runCatching {
        // Create the attendance record (validation happens in the domain model)
        val record = AttendanceRecord.create(
            raiderId = RaiderId(command.raiderId),
            guildId = GuildId(command.guildId),
            instance = command.instance,
            encounter = command.encounter,
            startDate = command.startDate,
            endDate = command.endDate,
            attendedRaids = command.attendedRaids,
            totalRaids = command.totalRaids
        )

        // Persist the record
        attendanceRepository.save(record)
    }
}

/**
 * Command for tracking attendance.
 *
 * @property raiderId The raider's unique identifier
 * @property guildId The guild's unique identifier
 * @property instance The raid instance name
 * @property encounter The specific encounter name (null for overall instance attendance)
 * @property startDate The start date of the attendance period
 * @property endDate The end date of the attendance period
 * @property attendedRaids Number of raids attended
 * @property totalRaids Total number of raids in the period
 */
data class TrackAttendanceCommand(
    val raiderId: Long,
    val guildId: String,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int
)
