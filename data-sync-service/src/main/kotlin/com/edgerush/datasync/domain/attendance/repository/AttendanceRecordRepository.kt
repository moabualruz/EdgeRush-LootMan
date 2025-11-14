package com.edgerush.datasync.domain.attendance.repository

import com.edgerush.datasync.domain.attendance.model.AttendanceRecord
import com.edgerush.datasync.domain.attendance.model.AttendanceRecordId
import com.edgerush.datasync.domain.attendance.model.GuildId
import com.edgerush.datasync.domain.attendance.model.RaiderId
import java.time.LocalDate

/**
 * Repository interface for attendance records.
 * Defines operations for persisting and retrieving attendance data.
 */
interface AttendanceRecordRepository {
    
    /**
     * Finds an attendance record by its ID.
     */
    fun findById(id: AttendanceRecordId): AttendanceRecord?
    
    /**
     * Finds all attendance records for a raider in a guild within a date range.
     */
    fun findByRaiderAndGuildInDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord>
    
    /**
     * Finds the most recent N attendance records for a raider in a guild.
     */
    fun findRecentByRaiderAndGuild(
        raiderId: RaiderId,
        guildId: GuildId,
        limit: Int
    ): List<AttendanceRecord>
    
    /**
     * Finds all attendance records for a guild within a date range.
     */
    fun findByGuildInDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord>
    
    /**
     * Saves an attendance record.
     */
    fun save(record: AttendanceRecord): AttendanceRecord
    
    /**
     * Deletes an attendance record by its ID.
     */
    fun delete(id: AttendanceRecordId)
}
