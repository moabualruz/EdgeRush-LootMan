package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.response.RaidbotsResultResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsResultEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RaidbotsResultMapper {

    fun toEntity(request: CreateRaidbotsResultRequest): RaidbotsResultEntity {
        return RaidbotsResultEntity(
            id = null,
            simulationId = request.simulationId ?: 0L,
            itemId = request.itemId ?: 0L,
            itemName = request.itemName ?: "",
            slot = request.slot ?: "",
            dpsGain = request.dpsGain ?: 0.0,
            percentGain = request.percentGain ?: 0.0,
            calculatedAt = request.calculatedAt ?: Instant.now(),
        )
    }

    fun updateEntity(entity: RaidbotsResultEntity, request: UpdateRaidbotsResultRequest): RaidbotsResultEntity {
        return entity.copy(
            simulationId = request.simulationId ?: entity.simulationId,
            itemId = request.itemId ?: entity.itemId,
            itemName = request.itemName ?: entity.itemName,
            slot = request.slot ?: entity.slot,
            dpsGain = request.dpsGain ?: entity.dpsGain,
            percentGain = request.percentGain ?: entity.percentGain,
            calculatedAt = request.calculatedAt ?: entity.calculatedAt,
        )
    }

    fun toResponse(entity: RaidbotsResultEntity): RaidbotsResultResponse {
        return RaidbotsResultResponse(
            id = entity.id!!,
            simulationId = entity.simulationId,
            itemId = entity.itemId,
            itemName = entity.itemName,
            slot = entity.slot,
            dpsGain = entity.dpsGain,
            percentGain = entity.percentGain,
            calculatedAt = entity.calculatedAt,
        )
    }
}
