package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateLootBanRequest
import com.edgerush.datasync.api.dto.request.UpdateLootBanRequest
import com.edgerush.datasync.api.dto.response.LootBanResponse
import com.edgerush.datasync.entity.LootBanEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class LootBanMapper {

    fun toEntity(request: CreateLootBanRequest): LootBanEntity {
        return LootBanEntity(
            id = null,
            guildId = request.guildId ?: "",
            characterName = request.characterName ?: "",
            reason = request.reason ?: "",
            bannedBy = request.bannedBy ?: "",
            bannedAt = request.bannedAt ?: LocalDateTime.now(),
            expiresAt = request.expiresAt,
        )
    }

    fun updateEntity(entity: LootBanEntity, request: UpdateLootBanRequest): LootBanEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            characterName = request.characterName ?: entity.characterName,
            reason = request.reason ?: entity.reason,
            bannedBy = request.bannedBy ?: entity.bannedBy,
            bannedAt = request.bannedAt ?: entity.bannedAt,
            expiresAt = request.expiresAt ?: entity.expiresAt,
        )
    }

    fun toResponse(entity: LootBanEntity): LootBanResponse {
        return LootBanResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            characterName = entity.characterName,
            reason = entity.reason,
            bannedBy = entity.bannedBy,
            bannedAt = entity.bannedAt,
            expiresAt = entity.expiresAt!!,
        )
    }
}
