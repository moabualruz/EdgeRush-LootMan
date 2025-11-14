package com.edgerush.datasync.domain.attendance.service

import com.edgerush.datasync.domain.attendance.model.AttendanceRecord
import com.edgerush.datasync.domain.attendance.model.AttendanceStats
import com.edgerush.datasync.domain.attendance.model.GuildId
import com.edgerush.datasync.domain.attendance.model.RaiderId
import com.edgerush.datasync.domain.attendance.repository.AttendanceRecordRepository
import java.time.LocalDate

/**
 * Domain service for calculating attendance statistics and scores.
 * Provides business logic for attendance-related calculations.
 */
class AttendanceCalculationService(
    private val attendanceRecordRepository: AttendanceRecordRepository
) {
    
    /**
     * Calculates attendance statistics for a raider within a date range.
     */
    fun calculateAttendanceStats(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): AttendanceStats {
        val records = attendanceRecordRepository.findByRaiderAndGuildInDateRange(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate
        )
        
        return calculateStatsFromRecords(records)
    }
    
    /**
     * Calculates recent attendance statistics for the last N raids.
     */
    fun calculateRecentAttendance(
        raiderId: RaiderId,
        guildId: GuildId,
        lastNRaids: Int
    ): AttendanceStats {
        val records = attendanceRecordRepository.findRecentByRaiderAndGuild(
            raiderId = raiderId,
            guildId = guildId,
            limit = lastNRaids
        )
        
        return calculateStatsFromRecords(records)
    }
    
    /**
     * Calculates an attendance score (0.0 to 1.0) based on attendance stats.
     * The score is the attendance percentage.
     */
    fun calculateAttendanceScore(stats: AttendanceStats): Double {
        return stats.attendancePercentage()
    }
    
    /**
     * Checks if a raider meets the attendance threshold within a date range.
     */
    fun meetsAttendanceThreshold(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
        threshold: Double
    ): Boolean {
        val stats = calculateAttendanceStats(raiderId, guildId, startDate, endDate)
        return stats.meetsAttendanceThreshold(threshold)
    }
    
    /**
     * Calculates attendance statistics from a list of attendance records.
     */
    private fun calculateStatsFromRecords(records: List<AttendanceRecord>): AttendanceStats {
        if (records.isEmpty()) {
            return AttendanceStats.empty()
        }
        
        val totalRaids = records.size
        val attendedRaids = records.count { it.isPresent() }
        val totalEncounters = records.size
        val selectedEncounters = records.count { it.isSelected() }
        
        return AttendanceStats.of(
            totalRaids = totalRaids,
            attendedRaids = attendedRaids,
            totalEncounters = totalEncounters,
            selectedEncounters = selectedEncounters
        )
    }
}
