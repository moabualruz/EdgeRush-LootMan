package com.edgerush.lootman.domain.attendance.model

import java.util.UUID

/**
 * Value object representing a unique attendance record identifier.
 */
data class AttendanceRecordId(val value: String) {
    init {
        require(value.isNotBlank()) { "Attendance Record ID cannot be blank" }
    }

    companion object {
        fun generate(): AttendanceRecordId = AttendanceRecordId(UUID.randomUUID().toString())
    }
}
