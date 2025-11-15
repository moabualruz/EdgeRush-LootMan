package com.edgerush.lootman.domain.attendance.service

import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Domain service for calculating attendance statistics.
 *
 * Provides methods to aggregate and calculate attendance metrics from attendance records.
 */
@Service
class AttendanceCalculationService(
    private val attendanceRepository: AttendanceRepository,
) {
    /**
     * Calculates overall attendance stats for a raider in a guild within a date range.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Aggregated attendance statistics
     */
    fun calculateAttendanceStats(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): AttendanceStats {
        val records =
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId,
                guildId,
                startDate,
                endDate,
            )

        return aggregateRecords(records)
    }

    /**
     * Calculates attendance stats for a raider in a specific instance.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param instance The raid instance name
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Aggregated attendance statistics for the instance
     */
    fun calculateAttendanceStatsForInstance(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): AttendanceStats {
        val records =
            attendanceRepository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
                raiderId,
                guildId,
                instance,
                startDate,
                endDate,
            )

        return aggregateRecords(records)
    }

    /**
     * Calculates attendance stats for a raider in a specific encounter.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param instance The raid instance name
     * @param encounter The encounter name
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Aggregated attendance statistics for the encounter
     */
    fun calculateAttendanceStatsForEncounter(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): AttendanceStats {
        val records =
            attendanceRepository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
                raiderId,
                guildId,
                instance,
                encounter,
                startDate,
                endDate,
            )

        return aggregateRecords(records)
    }

    /**
     * Calculates overall guild attendance stats within a date range.
     *
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Aggregated attendance statistics for the guild
     */
    fun calculateGuildAttendanceStats(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): AttendanceStats {
        val records =
            attendanceRepository.findByGuildIdAndDateRange(
                guildId,
                startDate,
                endDate,
            )

        return aggregateRecords(records)
    }

    /**
     * Aggregates multiple attendance records into a single AttendanceStats.
     *
     * @param records List of attendance records to aggregate
     * @return Aggregated attendance statistics
     */
    private fun aggregateRecords(records: List<com.edgerush.lootman.domain.attendance.model.AttendanceRecord>): AttendanceStats {
        if (records.isEmpty()) {
            return AttendanceStats.zero()
        }

        val totalRaids = records.sumOf { it.totalRaids }
        val attendedRaids = records.sumOf { it.attendedRaids }

        return AttendanceStats.calculate(attendedRaids, totalRaids)
    }
}
