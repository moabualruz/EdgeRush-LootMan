package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("historical_activity")
data class HistoricalActivityEntity(
    @Id
    val id: Long? = null,
    @Column("character_id")
    val characterId: Long?,
    @Column("character_name")
    val characterName: String,
    @Column("character_realm")
    val characterRealm: String?,
    @Column("period_id")
    val periodId: Long?,
    @Column("team_id")
    val teamId: Long?,
    @Column("season_id")
    val seasonId: Long?,
    @Column("data_json")
    val dataJson: String,
    @Column("synced_at")
    val syncedAt: OffsetDateTime = OffsetDateTime.now(),
)
