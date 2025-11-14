package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateBehavioralActionRequest
import com.edgerush.datasync.api.dto.request.UpdateBehavioralActionRequest
import com.edgerush.datasync.api.dto.response.BehavioralActionResponse
import com.edgerush.datasync.entity.BehavioralActionEntity
import org.springframework.stereotype.Component

@Component
class BehavioralActionMapper {
    fun toEntity(request: CreateBehavioralActionRequest): BehavioralActionEntity {
        return BehavioralActionEntity(
            id = null,
            guildId = request.guildId ?: "",
            characterName = request.characterName ?: "",
            actionType = request.actionType ?: "",
            deductionAmount = request.deductionAmount ?: 0.0,
            reason = "", // System populated
            appliedBy = "", // System populated
            appliedAt = java.time.LocalDateTime.now(), // System populated
            expiresAt = null, // System populated
            isActive = true,
        )
    }

    fun updateEntity(
        entity: BehavioralActionEntity,
        request: UpdateBehavioralActionRequest,
    ): BehavioralActionEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            characterName = request.characterName ?: entity.characterName,
            actionType = request.actionType ?: entity.actionType,
            deductionAmount = request.deductionAmount ?: entity.deductionAmount,
        )
    }

    fun toResponse(entity: BehavioralActionEntity): BehavioralActionResponse {
        return BehavioralActionResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            characterName = entity.characterName,
            actionType = entity.actionType,
            deductionAmount = entity.deductionAmount,
        )
    }
}
