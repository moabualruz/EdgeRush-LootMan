package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("period_snapshots")
data class PeriodSnapshotEntity(
    @Id val id: Long? = null,
    @Column("team_id")
    val teamId: Long?,
    @Column("season_id")
    val seasonId: Long?,
    @Column("period_id")
    val periodId: Long?,
    @Column("current_period")
    val currentPeriod: Long?,
    @Column("fetched_at")
    val fetchedAt: OffsetDateTime,
)
