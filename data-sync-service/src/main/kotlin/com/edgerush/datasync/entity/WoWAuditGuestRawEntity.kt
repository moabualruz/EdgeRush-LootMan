package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_guests_raw")
data class WoWAuditGuestRawEntity(
    @Id
    val guestId: Long,
    val payload: String,
    val syncedAt: OffsetDateTime
)

