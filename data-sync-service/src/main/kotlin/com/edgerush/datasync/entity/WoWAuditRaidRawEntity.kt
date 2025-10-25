package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_raids_raw")
data class WoWAuditRaidRawEntity(
    @Id
    val raidId: Long,
    val summaryJson: String?,
    val detailJson: String?,
    val syncedAt: OffsetDateTime
)

