package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateTeamRaidDayRequest
import com.edgerush.datasync.api.dto.request.UpdateTeamRaidDayRequest
import com.edgerush.datasync.api.dto.response.TeamRaidDayResponse
import com.edgerush.datasync.entity.TeamRaidDayEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class TeamRaidDayMapper {
    fun toEntity(request: CreateTeamRaidDayRequest): TeamRaidDayEntity {
        return TeamRaidDayEntity(
            id = null,
            teamId = request.teamId ?: 0L,
            weekDay = request.weekDay,
            startTime = request.startTime,
            endTime = request.endTime,
            currentInstance = request.currentInstance,
            difficulty = request.difficulty,
            activeFrom = request.activeFrom,
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: TeamRaidDayEntity,
        request: UpdateTeamRaidDayRequest,
    ): TeamRaidDayEntity {
        return entity.copy(
            teamId = request.teamId ?: entity.teamId,
            weekDay = request.weekDay ?: entity.weekDay,
            startTime = request.startTime ?: entity.startTime,
            endTime = request.endTime ?: entity.endTime,
            currentInstance = request.currentInstance ?: entity.currentInstance,
            difficulty = request.difficulty ?: entity.difficulty,
            activeFrom = request.activeFrom ?: entity.activeFrom,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: TeamRaidDayEntity): TeamRaidDayResponse {
        return TeamRaidDayResponse(
            id = entity.id!!,
            teamId = entity.teamId,
            weekDay = entity.weekDay!!,
            startTime = entity.startTime!!,
            endTime = entity.endTime!!,
            currentInstance = entity.currentInstance!!,
            difficulty = entity.difficulty!!,
            activeFrom = entity.activeFrom!!,
            syncedAt = entity.syncedAt,
        )
    }
}
