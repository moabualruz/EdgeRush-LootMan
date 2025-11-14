package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Attendance Commitment Score (ACS).
 *
 * ACS measures a raider's commitment through raid attendance.
 * Normalized value between 0.0 and 1.0.
 */
data class AttendanceCommitmentScore private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Attendance Commitment Score must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        fun of(value: Double): AttendanceCommitmentScore = AttendanceCommitmentScore(value)

        fun zero(): AttendanceCommitmentScore = AttendanceCommitmentScore(0.0)

        fun max(): AttendanceCommitmentScore = AttendanceCommitmentScore(1.0)
    }
}
