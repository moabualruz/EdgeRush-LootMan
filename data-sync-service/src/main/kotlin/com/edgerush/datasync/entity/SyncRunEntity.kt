package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("sync_runs")
data class SyncRunEntity(
    @Id
    val id: Long? = null,
    val source: String,
    val status: String,
    val startedAt: OffsetDateTime,
    val completedAt: OffsetDateTime?,
    val message: String?,
)
