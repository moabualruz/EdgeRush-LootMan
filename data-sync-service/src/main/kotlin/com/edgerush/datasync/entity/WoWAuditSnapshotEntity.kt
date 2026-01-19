package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_snapshots")
data class WoWAuditSnapshotEntity(
    @Id
    val id: Long? = null,
    @Column("endpoint")
    val endpoint: String,
    @Column("raw_payload")
    val rawPayload: String,
    @Column("synced_at")
    val syncedAt: OffsetDateTime = OffsetDateTime.now(),
)
