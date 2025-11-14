package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("sync_runs")
data class SyncRunEntity(
    @Id
    val id: Long? = null,
    val source: String,
    val status: String,
    val startedAt: java.time.OffsetDateTime,
    val completedAt: java.time.OffsetDateTime?,
    val message: String?,
)
