package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("period_snapshots")
data class PeriodSnapshotEntity(
    @Id
    val id: Long? = null,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val currentPeriod: Long?,
    val fetchedAt: OffsetDateTime
)
