package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.response.WishlistSnapshotResponse
import com.edgerush.datasync.entity.WishlistSnapshotEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class WishlistSnapshotMapper {
    fun toEntity(request: CreateWishlistSnapshotRequest): WishlistSnapshotEntity {
        return WishlistSnapshotEntity(
            id = null,
            raiderId = request.raiderId,
            characterName = request.characterName ?: "",
            characterRealm = request.characterRealm ?: "",
            characterRegion = request.characterRegion,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            rawPayload = request.rawPayload ?: "",
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: WishlistSnapshotEntity,
        request: UpdateWishlistSnapshotRequest,
    ): WishlistSnapshotEntity {
        return entity.copy(
            raiderId = request.raiderId ?: entity.raiderId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            characterRegion = request.characterRegion ?: entity.characterRegion,
            teamId = request.teamId ?: entity.teamId,
            seasonId = request.seasonId ?: entity.seasonId,
            periodId = request.periodId ?: entity.periodId,
            rawPayload = request.rawPayload ?: entity.rawPayload,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: WishlistSnapshotEntity): WishlistSnapshotResponse {
        return WishlistSnapshotResponse(
            id = entity.id!!,
            raiderId = entity.raiderId!!,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm,
            characterRegion = entity.characterRegion!!,
            teamId = entity.teamId!!,
            seasonId = entity.seasonId!!,
            periodId = entity.periodId!!,
            rawPayload = entity.rawPayload,
            syncedAt = entity.syncedAt,
        )
    }
}
