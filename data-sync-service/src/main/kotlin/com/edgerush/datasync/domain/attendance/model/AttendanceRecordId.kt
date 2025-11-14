package com.edgerush.datasync.domain.attendance.model

import java.util.concurrent.atomic.AtomicLong

/**
 * Value object representing an attendance record identifier.
 */
data class AttendanceRecordId(val value: Long) {
    companion object {
        private val counter = AtomicLong(0)
        
        fun generate(): AttendanceRecordId = AttendanceRecordId(counter.incrementAndGet())
    }
}
