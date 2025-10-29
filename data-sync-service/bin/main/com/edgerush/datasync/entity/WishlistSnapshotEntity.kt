package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wishlist_snapshots")
data class WishlistSnapshotEntity(
    @Id
    val id: Long? = null,
    val raiderId: Long?,
    val characterName: String,
    val characterRealm: String,
    val characterRegion: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val rawPayload: String,
    val syncedAt: OffsetDateTime
)
