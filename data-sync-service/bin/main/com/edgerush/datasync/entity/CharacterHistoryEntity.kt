package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("character_history")
data class CharacterHistoryEntity(
    @Id
    val id: Long? = null,

    @Column("character_id")
    val characterId: Long,

    @Column("character_name")
    val characterName: String,

    @Column("character_realm")
    val characterRealm: String?,

    @Column("character_region")
    val characterRegion: String?,

    @Column("team_id")
    val teamId: Long?,

    @Column("season_id")
    val seasonId: Long?,

    @Column("period_id")
    val periodId: Long?,

    @Column("history_json")
    val historyJson: String,

    @Column("best_gear_json")
    val bestGearJson: String?,

    @Column("synced_at")
    val syncedAt: OffsetDateTime = OffsetDateTime.now()
)