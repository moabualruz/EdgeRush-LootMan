package com.edgerush.lootman.api.wishlist

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateWishlistSnapshotRequest(
    val raiderId: Long? = null,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    @field:NotBlank(message = "Character realm is required")
    val characterRealm: String,
    val characterRegion: String? = null,
    val teamId: Long? = null,
    val seasonId: Long? = null,
    val periodId: Long? = null,
    @field:NotBlank(message = "Raw payload is required")
    val rawPayload: String,
)

data class UpdateWishlistSnapshotRequest(
    val rawPayload: String? = null,
)

data class WishlistSnapshotResponse(
    val id: Long,
    val raiderId: Long?,
    val characterName: String,
    val characterRealm: String,
    val characterRegion: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val rawPayload: String,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(e: WishlistSnapshotEntity) = WishlistSnapshotResponse(
            e.id!!, e.raiderId, e.characterName, e.characterRealm, e.characterRegion,
            e.teamId, e.seasonId, e.periodId, e.rawPayload, e.syncedAt
        )
    }
}

data class WishlistSnapshotExistsResponse(val exists: Boolean)
data class WishlistSnapshotCountResponse(val count: Long)
