package com.edgerush.lootman.domain.attendance.model

import java.time.Instant
import java.time.LocalDate

/**
 * Entity representing an attendance record for a raider.
 *
 * Tracks attendance for a specific raider in a guild, optionally for a specific
 * raid instance and encounter, over a date range.
 */
@ConsistentCopyVisibility
data class AttendanceRecord private constructor(
    val id: AttendanceRecordId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int,
    val recordedAt: Instant
) {
    /**
     * Calculated attendance percentage.
     */
    val attendancePercentage: Double
        get() = if (totalRaids == 0) 0.0 else attendedRaids.toDouble() / totalRaids.toDouble()

    companion object {
        /**
         * Creates a new AttendanceRecord.
         *
         * @param raiderId The raider's unique identifier
         * @param guildId The guild's unique identifier
         * @param instance The raid instance name
         * @param encounter The specific encounter name (null for overall instance attendance)
         * @param startDate The start date of the attendance period
         * @param endDate The end date of the attendance period
         * @param attendedRaids Number of raids attended
         * @param totalRaids Total number of raids in the period
         * @return A new AttendanceRecord instance
         */
        fun create(
            raiderId: RaiderId,
            guildId: GuildId,
            instance: String,
            encounter: String?,
            startDate: LocalDate,
            endDate: LocalDate,
            attendedRaids: Int,
            totalRaids: Int
        ): AttendanceRecord {
            require(instance.isNotBlank()) {
                "Instance name cannot be blank"
            }
            require(attendedRaids >= 0) {
                "Attended raids cannot be negative, got $attendedRaids"
            }
            require(totalRaids > 0) {
                "Total raids must be positive, got $totalRaids"
            }
            require(attendedRaids <= totalRaids) {
                "Attended raids ($attendedRaids) cannot exceed total raids ($totalRaids)"
            }
            require(!endDate.isBefore(startDate)) {
                "End date ($endDate) cannot be before start date ($startDate)"
            }

            return AttendanceRecord(
                id = AttendanceRecordId.generate(),
                raiderId = raiderId,
                guildId = guildId,
                instance = instance,
                encounter = encounter,
                startDate = startDate,
                endDate = endDate,
                attendedRaids = attendedRaids,
                totalRaids = totalRaids,
                recordedAt = Instant.now()
            )
        }
    }
}
