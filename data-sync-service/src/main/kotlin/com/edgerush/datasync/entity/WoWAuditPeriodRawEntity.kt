package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_period_raw")
data class WoWAuditPeriodRawEntity(
    @Id
    val periodId: Long,
    val payload: String,
    val syncedAt: OffsetDateTime
)

