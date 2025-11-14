package com.edgerush.lootman.domain.attendance.model

/**
 * Value object representing attendance statistics.
 *
 * Encapsulates attendance metrics including percentage, total raids, attended raids, and missed raids.
 * All values are validated to ensure consistency.
 */
@ConsistentCopyVisibility
data class AttendanceStats private constructor(
    val attendancePercentage: Double,
    val totalRaids: Int,
    val attendedRaids: Int,
    val missedRaids: Int
) {
    init {
        require(attendancePercentage in 0.0..1.0) {
            "Attendance percentage must be between 0.0 and 1.0, got $attendancePercentage"
        }
        require(totalRaids >= 0) {
            "Total raids cannot be negative, got $totalRaids"
        }
        require(attendedRaids >= 0) {
            "Attended raids cannot be negative, got $attendedRaids"
        }
        require(missedRaids >= 0) {
            "Missed raids cannot be negative, got $missedRaids"
        }
        require(attendedRaids + missedRaids == totalRaids) {
            "Attended ($attendedRaids) + Missed ($missedRaids) must equal Total ($totalRaids)"
        }
    }

    companion object {
        /**
         * Creates AttendanceStats with explicit values.
         *
         * @param attendancePercentage The attendance percentage (0.0 to 1.0)
         * @param totalRaids Total number of raids
         * @param attendedRaids Number of raids attended
         * @param missedRaids Number of raids missed
         * @return A new AttendanceStats instance
         */
        fun of(
            attendancePercentage: Double,
            totalRaids: Int,
            attendedRaids: Int,
            missedRaids: Int
        ): AttendanceStats = AttendanceStats(
            attendancePercentage,
            totalRaids,
            attendedRaids,
            missedRaids
        )

        /**
         * Calculates AttendanceStats from attended and total raids.
         *
         * @param attendedRaids Number of raids attended
         * @param totalRaids Total number of raids
         * @return A new AttendanceStats instance with calculated percentage
         */
        fun calculate(attendedRaids: Int, totalRaids: Int): AttendanceStats {
            require(attendedRaids >= 0) {
                "Attended raids cannot be negative, got $attendedRaids"
            }
            require(totalRaids >= 0) {
                "Total raids cannot be negative, got $totalRaids"
            }
            require(attendedRaids <= totalRaids) {
                "Attended raids ($attendedRaids) cannot exceed total raids ($totalRaids)"
            }

            val percentage = if (totalRaids == 0) 0.0 else attendedRaids.toDouble() / totalRaids.toDouble()
            val missed = totalRaids - attendedRaids

            return AttendanceStats(
                attendancePercentage = percentage,
                totalRaids = totalRaids,
                attendedRaids = attendedRaids,
                missedRaids = missed
            )
        }

        /**
         * Creates AttendanceStats with all zero values.
         *
         * @return An AttendanceStats instance with zero values
         */
        fun zero(): AttendanceStats = AttendanceStats(
            attendancePercentage = 0.0,
            totalRaids = 0,
            attendedRaids = 0,
            missedRaids = 0
        )
    }
}
