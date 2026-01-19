package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("sync_runs")
data class SyncRunEntity(
    @Id
    val id: Long? = null,
    @Column("source")
    val source: String,
    @Column("status")
    val status: String,
    @Column("started_at")
    val startedAt: OffsetDateTime,
    @Column("completed_at")
    val completedAt: OffsetDateTime?,
    @Column("message")
    val message: String?,
)
