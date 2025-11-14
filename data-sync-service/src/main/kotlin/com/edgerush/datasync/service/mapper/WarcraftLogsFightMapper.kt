package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsFightResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsFightEntity
import org.springframework.stereotype.Component

@Component
class WarcraftLogsFightMapper {

    fun toEntity(request: CreateWarcraftLogsFightRequest): WarcraftLogsFightEntity {
        return WarcraftLogsFightEntity(
            id = null,
            reportId = request.reportId ?: 0L,
            fightId = request.fightId ?: 0,
            encounterId = request.encounterId ?: 0,
            encounterName = request.encounterName ?: "",
            difficulty = request.difficulty ?: "",
            kill = request.kill ?: false,
            startTime = request.startTime ?: 0L,
            endTime = request.endTime ?: 0L,
            bossPercentage = request.bossPercentage,
        )
    }

    fun updateEntity(entity: WarcraftLogsFightEntity, request: UpdateWarcraftLogsFightRequest): WarcraftLogsFightEntity {
        return entity.copy(
            reportId = request.reportId ?: entity.reportId,
            fightId = request.fightId ?: entity.fightId,
            encounterId = request.encounterId ?: entity.encounterId,
            encounterName = request.encounterName ?: entity.encounterName,
            difficulty = request.difficulty ?: entity.difficulty,
            kill = request.kill ?: entity.kill,
            startTime = request.startTime ?: entity.startTime,
            endTime = request.endTime ?: entity.endTime,
            bossPercentage = request.bossPercentage ?: entity.bossPercentage,
        )
    }

    fun toResponse(entity: WarcraftLogsFightEntity): WarcraftLogsFightResponse {
        return WarcraftLogsFightResponse(
            id = entity.id!!,
            reportId = entity.reportId,
            fightId = entity.fightId,
            encounterId = entity.encounterId,
            encounterName = entity.encounterName,
            difficulty = entity.difficulty,
            kill = entity.kill,
            startTime = entity.startTime,
            endTime = entity.endTime,
            bossPercentage = entity.bossPercentage!!,
        )
    }
}
