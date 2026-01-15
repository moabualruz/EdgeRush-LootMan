package com.edgerush.lootman.application.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Use case for getting a specific attendance record by ID.
 */
@Service
class GetAttendanceRecordUseCase(
    private val attendanceRepository: AttendanceRepository,
) {
    fun execute(query: GetAttendanceRecordQuery): Result<AttendanceRecord> =
        runCatching {
            attendanceRepository.findById(AttendanceRecordId(query.recordId))
                ?: throw NoSuchElementException("Attendance record not found: ${query.recordId}")
        }
}

/**
 * Use case for updating an attendance record.
 */
@Service
class UpdateAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository,
) {
    fun execute(command: UpdateAttendanceCommand): Result<AttendanceRecord> =
        runCatching {
            val existingRecord =
                attendanceRepository.findById(AttendanceRecordId(command.recordId))
                    ?: throw NoSuchElementException("Attendance record not found: ${command.recordId}")

            // Create a new record with updated fields (AttendanceRecord is immutable)
            val updatedRecord =
                AttendanceRecord.create(
                    raiderId = existingRecord.raiderId,
                    guildId = existingRecord.guildId,
                    instance = command.instance ?: existingRecord.instance,
                    encounter = command.encounter ?: existingRecord.encounter,
                    startDate = command.startDate ?: existingRecord.startDate,
                    endDate = command.endDate ?: existingRecord.endDate,
                    attendedRaids = command.attendedRaids ?: existingRecord.attendedRaids,
                    totalRaids = command.totalRaids ?: existingRecord.totalRaids,
                )

            // Delete old record and save new one (since ID is immutable)
            attendanceRepository.delete(AttendanceRecordId(command.recordId))
            attendanceRepository.save(updatedRecord)
        }
}

/**
 * Use case for deleting an attendance record.
 */
@Service
class DeleteAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository,
) {
    fun execute(command: DeleteAttendanceCommand): Result<Unit> =
        runCatching {
            val recordId = AttendanceRecordId(command.recordId)
            attendanceRepository.findById(recordId)
                ?: throw NoSuchElementException("Attendance record not found: ${command.recordId}")
            attendanceRepository.delete(recordId)
        }
}

/**
 * Use case for listing all attendance records for a raider.
 */
@Service
class ListRaiderAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository,
) {
    fun execute(query: ListRaiderAttendanceQuery): Result<List<AttendanceRecord>> =
        runCatching {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId = RaiderId(query.raiderId),
                guildId = GuildId(query.guildId),
                startDate = query.startDate,
                endDate = query.endDate,
            )
        }
}

/**
 * Use case for getting guild attendance summary.
 */
@Service
class GetGuildAttendanceSummaryUseCase(
    private val attendanceRepository: AttendanceRepository,
) {
    fun execute(query: GetGuildAttendanceSummaryQuery): Result<GuildAttendanceSummary> =
        runCatching {
            val records =
                attendanceRepository.findByGuildIdAndDateRange(
                    guildId = GuildId(query.guildId),
                    startDate = query.startDate,
                    endDate = query.endDate,
                )

            // Group by raider and calculate average attendance
            val raiderStats =
                records
                    .groupBy { it.raiderId }
                    .map { (raiderId, raiderRecords) ->
                        val totalAttended = raiderRecords.sumOf { it.attendedRaids }
                        val totalRaids = raiderRecords.sumOf { it.totalRaids }
                        val percentage = if (totalRaids > 0) totalAttended.toDouble() / totalRaids else 0.0
                        RaiderAttendanceSummary(
                            raiderId = raiderId.value,
                            totalRecords = raiderRecords.size,
                            totalAttendedRaids = totalAttended,
                            totalRaids = totalRaids,
                            averageAttendancePercentage = percentage,
                        )
                    }

            val overallTotalAttended = records.sumOf { it.attendedRaids }
            val overallTotalRaids = records.sumOf { it.totalRaids }
            val overallPercentage = if (overallTotalRaids > 0) overallTotalAttended.toDouble() / overallTotalRaids else 0.0

            GuildAttendanceSummary(
                guildId = query.guildId,
                startDate = query.startDate,
                endDate = query.endDate,
                totalRecords = records.size,
                uniqueRaiders = raiderStats.size,
                overallAttendancePercentage = overallPercentage,
                raiderSummaries = raiderStats,
            )
        }
}

// Query and Command classes

data class GetAttendanceRecordQuery(
    val recordId: String,
)

data class UpdateAttendanceCommand(
    val recordId: String,
    val instance: String? = null,
    val encounter: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val attendedRaids: Int? = null,
    val totalRaids: Int? = null,
)

data class DeleteAttendanceCommand(
    val recordId: String,
)

data class ListRaiderAttendanceQuery(
    val raiderId: Long,
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class GetGuildAttendanceSummaryQuery(
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

// Response models

data class GuildAttendanceSummary(
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalRecords: Int,
    val uniqueRaiders: Int,
    val overallAttendancePercentage: Double,
    val raiderSummaries: List<RaiderAttendanceSummary>,
)

data class RaiderAttendanceSummary(
    val raiderId: Long,
    val totalRecords: Int,
    val totalAttendedRaids: Int,
    val totalRaids: Int,
    val averageAttendancePercentage: Double,
)
