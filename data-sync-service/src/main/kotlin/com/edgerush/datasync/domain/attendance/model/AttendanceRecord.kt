package com.edgerush.datasync.domain.attendance.model

import java.time.LocalDate

/**
 * Entity representing a raider's attendance for a specific raid.
 * Tracks whether the raider was present and selected for the raid.
 */
@ConsistentCopyVisibility
data class AttendanceRecord private constructor(
    val id: AttendanceRecordId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val raidDate: LocalDate,
    val wasPresent: Boolean,
    val wasSelected: Boolean,
    val instance: String? = null,
    val difficulty: String? = null
) {
    companion object {
        /**
         * Creates a new attendance record.
         */
        fun create(
            raiderId: RaiderId,
            guildId: GuildId,
            raidDate: LocalDate,
            wasPresent: Boolean,
            wasSelected: Boolean,
            instance: String? = null,
            difficulty: String? = null
        ): AttendanceRecord {
            return AttendanceRecord(
                id = AttendanceRecordId.generate(),
                raiderId = raiderId,
                guildId = guildId,
                raidDate = raidDate,
                wasPresent = wasPresent,
                wasSelected = wasSelected,
                instance = instance,
                difficulty = difficulty
            )
        }
        
        /**
         * Reconstitutes an attendance record from persistence.
         */
        fun reconstitute(
            id: AttendanceRecordId,
            raiderId: RaiderId,
            guildId: GuildId,
            raidDate: LocalDate,
            wasPresent: Boolean,
            wasSelected: Boolean,
            instance: String? = null,
            difficulty: String? = null
        ): AttendanceRecord {
            return AttendanceRecord(
                id = id,
                raiderId = raiderId,
                guildId = guildId,
                raidDate = raidDate,
                wasPresent = wasPresent,
                wasSelected = wasSelected,
                instance = instance,
                difficulty = difficulty
            )
        }
    }
    
    /**
     * Checks if the raider was present for this raid.
     */
    fun isPresent(): Boolean = wasPresent
    
    /**
     * Checks if the raider was selected for this raid.
     */
    fun isSelected(): Boolean = wasSelected
    
    /**
     * Checks if the raider was absent from this raid.
     */
    fun isAbsent(): Boolean = !wasPresent
}
