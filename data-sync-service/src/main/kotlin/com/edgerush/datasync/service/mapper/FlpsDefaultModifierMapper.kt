package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsDefaultModifierResponse
import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class FlpsDefaultModifierMapper {
    fun toEntity(request: CreateFlpsDefaultModifierRequest): FlpsDefaultModifierEntity {
        return FlpsDefaultModifierEntity(
            id = null,
            category = request.category ?: "",
            modifierKey = request.modifierKey ?: "",
            modifierValue = request.modifierValue ?: java.math.BigDecimal.ZERO,
            description = request.description,
            createdAt = OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: FlpsDefaultModifierEntity,
        request: UpdateFlpsDefaultModifierRequest,
    ): FlpsDefaultModifierEntity {
        return entity.copy(
            category = request.category ?: entity.category,
            modifierKey = request.modifierKey ?: entity.modifierKey,
            modifierValue = request.modifierValue ?: entity.modifierValue,
            description = request.description ?: entity.description,
        )
    }

    fun toResponse(entity: FlpsDefaultModifierEntity): FlpsDefaultModifierResponse {
        return FlpsDefaultModifierResponse(
            id = entity.id!!,
            category = entity.category,
            modifierKey = entity.modifierKey,
            modifierValue = entity.modifierValue,
            description = entity.description!!,
            createdAt = entity.createdAt,
        )
    }
}
