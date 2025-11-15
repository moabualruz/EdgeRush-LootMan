package com.edgerush.lootman.domain.attendance.repository

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import java.time.LocalDate

/**
 * Repository interface for AttendanceRecord domain entities.
 *
 * Defines the contract for persistence operations on attendance records.
 */
interface AttendanceRepository {
    /**
     * Finds an attendance record by its unique identifier.
     *
     * @param id The attendance record ID
     * @return The attendance record if found, null otherwise
     */
    fun findById(id: AttendanceRecordId): AttendanceRecord?

    /**
     * Finds all attendance records for a raider in a guild within a date range.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of attendance records
     */
    fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord>

    /**
     * Finds attendance records for a raider in a specific instance within a date range.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param instance The raid instance name
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of attendance records
     */
    fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord>

    /**
     * Finds attendance records for a raider in a specific encounter within a date range.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param instance The raid instance name
     * @param encounter The encounter name
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of attendance records
     */
    fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord>

    /**
     * Finds all attendance records for a guild within a date range.
     *
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of attendance records
     */
    fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord>

    /**
     * Saves an attendance record.
     *
     * @param record The attendance record to save
     * @return The saved attendance record
     */
    fun save(record: AttendanceRecord): AttendanceRecord

    /**
     * Deletes an attendance record.
     *
     * @param id The attendance record ID to delete
     */
    fun delete(id: AttendanceRecordId)
}
