package com.edgerush.datasync.domain.attendance.model

/**
 * Value object representing attendance statistics for a raider.
 * Provides calculations for attendance and selection percentages.
 */
@ConsistentCopyVisibility
data class AttendanceStats private constructor(
    val totalRaids: Int,
    val attendedRaids: Int,
    val totalEncounters: Int,
    val selectedEncounters: Int
) {
    init {
        require(totalRaids >= 0) { "Total raids must be non-negative, got: $totalRaids" }
        require(attendedRaids >= 0) { "Attended raids must be non-negative, got: $attendedRaids" }
        require(totalEncounters >= 0) { "Total encounters must be non-negative, got: $totalEncounters" }
        require(selectedEncounters >= 0) { "Selected encounters must be non-negative, got: $selectedEncounters" }
        require(attendedRaids <= totalRaids) { 
            "Attended raids ($attendedRaids) cannot exceed total raids ($totalRaids)" 
        }
        require(selectedEncounters <= totalEncounters) { 
            "Selected encounters ($selectedEncounters) cannot exceed total encounters ($totalEncounters)" 
        }
    }
    
    companion object {
        /**
         * Creates attendance stats with the given values.
         */
        fun of(
            totalRaids: Int,
            attendedRaids: Int,
            totalEncounters: Int,
            selectedEncounters: Int
        ): AttendanceStats {
            return AttendanceStats(
                totalRaids = totalRaids,
                attendedRaids = attendedRaids,
                totalEncounters = totalEncounters,
                selectedEncounters = selectedEncounters
            )
        }
        
        /**
         * Creates empty attendance stats (all zeros).
         */
        fun empty(): AttendanceStats {
            return AttendanceStats(
                totalRaids = 0,
                attendedRaids = 0,
                totalEncounters = 0,
                selectedEncounters = 0
            )
        }
    }
    
    /**
     * Calculates the attendance percentage (attended / total raids).
     * Returns 0.0 if total raids is 0.
     */
    fun attendancePercentage(): Double {
        return if (totalRaids == 0) 0.0 else attendedRaids.toDouble() / totalRaids.toDouble()
    }
    
    /**
     * Calculates the selection percentage (selected / total encounters).
     * Returns 0.0 if total encounters is 0.
     */
    fun selectionPercentage(): Double {
        return if (totalEncounters == 0) 0.0 else selectedEncounters.toDouble() / totalEncounters.toDouble()
    }
    
    /**
     * Checks if the raider has perfect attendance (100%).
     */
    fun isPerfectAttendance(): Boolean {
        return totalRaids > 0 && attendedRaids == totalRaids && 
               totalEncounters > 0 && selectedEncounters == totalEncounters
    }
    
    /**
     * Checks if the attendance percentage meets or exceeds the given threshold.
     */
    fun meetsAttendanceThreshold(threshold: Double): Boolean {
        return attendancePercentage() >= threshold
    }
    
    /**
     * Combines this attendance stats with another, summing all values.
     */
    fun combine(other: AttendanceStats): AttendanceStats {
        return AttendanceStats(
            totalRaids = this.totalRaids + other.totalRaids,
            attendedRaids = this.attendedRaids + other.attendedRaids,
            totalEncounters = this.totalEncounters + other.totalEncounters,
            selectedEncounters = this.selectedEncounters + other.selectedEncounters
        )
    }
}
