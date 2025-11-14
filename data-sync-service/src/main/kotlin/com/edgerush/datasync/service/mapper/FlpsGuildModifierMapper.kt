package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsGuildModifierResponse
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class FlpsGuildModifierMapper {

    fun toEntity(request: CreateFlpsGuildModifierRequest): FlpsGuildModifierEntity {
        return FlpsGuildModifierEntity(
            id = null,
            guildId = request.guildId ?: "",
            category = request.category ?: "",
            modifierKey = request.modifierKey ?: "",
            modifierValue = request.modifierValue ?: java.math.BigDecimal.ZERO,
            description = request.description,
            createdAt = OffsetDateTime.now(),
        )
    }

    fun updateEntity(entity: FlpsGuildModifierEntity, request: UpdateFlpsGuildModifierRequest): FlpsGuildModifierEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            category = request.category ?: entity.category,
            modifierKey = request.modifierKey ?: entity.modifierKey,
            modifierValue = request.modifierValue ?: entity.modifierValue,
            description = request.description ?: entity.description,
        )
    }

    fun toResponse(entity: FlpsGuildModifierEntity): FlpsGuildModifierResponse {
        return FlpsGuildModifierResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            category = entity.category,
            modifierKey = entity.modifierKey,
            modifierValue = entity.modifierValue,
            description = entity.description!!,
            createdAt = entity.createdAt,
        )
    }
}
