package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_attendance_raw")
data class WoWAuditAttendanceRawEntity(
    @Id
    val id: Long,
    val payload: String,
    val syncedAt: OffsetDateTime
)

