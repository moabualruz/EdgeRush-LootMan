package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.response.PeriodSnapshotResponse
import com.edgerush.datasync.entity.PeriodSnapshotEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class PeriodSnapshotMapper {

    fun toEntity(request: CreatePeriodSnapshotRequest): PeriodSnapshotEntity {
        return PeriodSnapshotEntity(
            id = null,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            currentPeriod = request.currentPeriod,
            fetchedAt = request.fetchedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(entity: PeriodSnapshotEntity, request: UpdatePeriodSnapshotRequest): PeriodSnapshotEntity {
        return entity.copy(
            teamId = request.teamId ?: entity.teamId,
            seasonId = request.seasonId ?: entity.seasonId,
            periodId = request.periodId ?: entity.periodId,
            currentPeriod = request.currentPeriod ?: entity.currentPeriod,
            fetchedAt = request.fetchedAt ?: entity.fetchedAt,
        )
    }

    fun toResponse(entity: PeriodSnapshotEntity): PeriodSnapshotResponse {
        return PeriodSnapshotResponse(
            id = entity.id!!,
            teamId = entity.teamId!!,
            seasonId = entity.seasonId!!,
            periodId = entity.periodId!!,
            currentPeriod = entity.currentPeriod!!,
            fetchedAt = entity.fetchedAt,
        )
    }
}
