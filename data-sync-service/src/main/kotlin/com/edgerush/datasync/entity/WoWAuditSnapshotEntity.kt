package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_snapshots")
data class WoWAuditSnapshotEntity(
    @Id
    val id: Long? = null,
    val endpoint: String,
    val rawPayload: String,
    val syncedAt: OffsetDateTime,
)
