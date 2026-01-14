package com.edgerush.lootman.api.snapshot

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import java.time.OffsetDateTime

data class CreatePeriodSnapshotRequest(val teamId: Long? = null, val seasonId: Long? = null, val periodId: Long? = null, val currentPeriod: Long? = null)
data class UpdatePeriodSnapshotRequest(val currentPeriod: Long? = null)
data class PeriodSnapshotResponse(val id: Long, val teamId: Long?, val seasonId: Long?, val periodId: Long?, val currentPeriod: Long?, val fetchedAt: OffsetDateTime) {
    companion object { fun from(e: PeriodSnapshotEntity) = PeriodSnapshotResponse(e.id!!, e.teamId, e.seasonId, e.periodId, e.currentPeriod, e.fetchedAt) }
}
data class PeriodSnapshotExistsResponse(val exists: Boolean)
data class PeriodSnapshotCountResponse(val count: Long)
