package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidRequest
import com.edgerush.datasync.api.dto.response.RaidResponse
import com.edgerush.datasync.entity.RaidEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RaidMapper {
    
    fun toEntity(request: CreateRaidRequest): RaidEntity {
        return RaidEntity(
            raidId = 0, // Will be generated
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            instance = request.instance,
            difficulty = request.difficulty,
            optional = request.optional,
            status = request.status,
            presentSize = null,
            totalSize = null,
            notes = request.notes,
            selectionsImage = null,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            syncedAt = OffsetDateTime.now()
        )
    }
    
    fun updateEntity(entity: RaidEntity, request: UpdateRaidRequest): RaidEntity {
        return entity.copy(
            date = request.date ?: entity.date,
            startTime = request.startTime ?: entity.startTime,
            endTime = request.endTime ?: entity.endTime,
            instance = request.instance ?: entity.instance,
            difficulty = request.difficulty ?: entity.difficulty,
            optional = request.optional ?: entity.optional,
            status = request.status ?: entity.status,
            notes = request.notes ?: entity.notes,
            updatedAt = OffsetDateTime.now(),
            syncedAt = OffsetDateTime.now()
        )
    }
    
    fun toResponse(entity: RaidEntity): RaidResponse {
        return RaidResponse(
            raidId = entity.raidId,
            date = entity.date,
            startTime = entity.startTime,
            endTime = entity.endTime,
            instance = entity.instance,
            difficulty = entity.difficulty,
            optional = entity.optional,
            status = entity.status,
            presentSize = entity.presentSize,
            totalSize = entity.totalSize,
            notes = entity.notes,
            teamId = entity.teamId,
            seasonId = entity.seasonId,
            periodId = entity.periodId,
            syncedAt = entity.syncedAt
        )
    }
}
