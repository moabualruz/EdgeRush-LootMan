package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaidSignupRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidSignupRequest
import com.edgerush.datasync.api.dto.response.RaidSignupResponse
import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.stereotype.Component

@Component
class RaidSignupMapper {
    fun toEntity(request: CreateRaidSignupRequest): RaidSignupEntity {
        return RaidSignupEntity(
            id = null,
            raidId = request.raidId ?: 0L,
            characterId = request.characterId,
            characterName = request.characterName,
            characterRealm = request.characterRealm,
            characterRegion = request.characterRegion,
            characterClass = request.characterClass,
            characterRole = request.characterRole,
            characterGuest = request.characterGuest,
            status = request.status,
            comment = request.comment,
            selected = request.selected,
        )
    }

    fun updateEntity(
        entity: RaidSignupEntity,
        request: UpdateRaidSignupRequest,
    ): RaidSignupEntity {
        return entity.copy(
            raidId = request.raidId ?: entity.raidId,
            characterId = request.characterId ?: entity.characterId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            characterRegion = request.characterRegion ?: entity.characterRegion,
            characterClass = request.characterClass ?: entity.characterClass,
            characterRole = request.characterRole ?: entity.characterRole,
            characterGuest = request.characterGuest ?: entity.characterGuest,
            status = request.status ?: entity.status,
            comment = request.comment ?: entity.comment,
            selected = request.selected ?: entity.selected,
        )
    }

    fun toResponse(entity: RaidSignupEntity): RaidSignupResponse {
        return RaidSignupResponse(
            id = entity.id!!,
            raidId = entity.raidId,
            characterId = entity.characterId!!,
            characterName = entity.characterName!!,
            characterRealm = entity.characterRealm!!,
            characterRegion = entity.characterRegion!!,
            characterClass = entity.characterClass!!,
            characterRole = entity.characterRole!!,
            characterGuest = entity.characterGuest!!,
            status = entity.status!!,
            comment = entity.comment!!,
            selected = entity.selected!!,
        )
    }
}
