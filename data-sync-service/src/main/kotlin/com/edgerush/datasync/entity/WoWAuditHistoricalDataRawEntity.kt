package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_historical_data_raw")
data class WoWAuditHistoricalDataRawEntity(
    @Id
    val periodId: Long,
    val payload: String,
    val syncedAt: OffsetDateTime
)

