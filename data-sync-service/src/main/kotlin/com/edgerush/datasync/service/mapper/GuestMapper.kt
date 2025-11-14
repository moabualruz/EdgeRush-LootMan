package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateGuestRequest
import com.edgerush.datasync.api.dto.request.UpdateGuestRequest
import com.edgerush.datasync.api.dto.response.GuestResponse
import com.edgerush.datasync.entity.GuestEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class GuestMapper {
    
    fun toEntity(request: CreateGuestRequest, guestId: Long): GuestEntity {
        return GuestEntity(
            guestId = guestId,
            name = request.name,
            realm = request.realm,
            `class` = request.`class`,
            role = request.role,
            blizzardId = request.blizzardId,
            trackingSince = request.trackingSince,
            syncedAt = OffsetDateTime.now()
        )
    }
    
    fun toEntity(existing: GuestEntity, request: UpdateGuestRequest): GuestEntity {
        return existing.copy(
            name = request.name ?: existing.name,
            realm = request.realm ?: existing.realm,
            `class` = request.`class` ?: existing.`class`,
            role = request.role ?: existing.role,
            blizzardId = request.blizzardId ?: existing.blizzardId,
            trackingSince = request.trackingSince ?: existing.trackingSince,
            syncedAt = OffsetDateTime.now()
        )
    }
    
    fun toResponse(entity: GuestEntity): GuestResponse {
        return GuestResponse(
            guestId = entity.guestId,
            name = entity.name,
            realm = entity.realm,
            `class` = entity.`class`,
            role = entity.role,
            blizzardId = entity.blizzardId,
            trackingSince = entity.trackingSince,
            syncedAt = entity.syncedAt
        )
    }
}
