package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wishlist_snapshots")
data class WishlistSnapshotEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long?,
    @Column("character_name")
    val characterName: String,
    @Column("character_realm")
    val characterRealm: String,
    @Column("character_region")
    val characterRegion: String?,
    @Column("team_id")
    val teamId: Long?,
    @Column("season_id")
    val seasonId: Long?,
    @Column("period_id")
    val periodId: Long?,
    @Column("raw_payload")
    val rawPayload: String,
    @Column("synced_at")
    val syncedAt: OffsetDateTime = OffsetDateTime.now(),
)
