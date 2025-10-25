package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_applications_raw")
data class WoWAuditApplicationRawEntity(
    @Id
    val applicationId: Long,
    val summaryJson: String?,
    val detailJson: String?,
    val syncedAt: OffsetDateTime
)

