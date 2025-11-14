package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderPvpBracketRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderPvpBracketRequest
import com.edgerush.datasync.api.dto.response.RaiderPvpBracketResponse
import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import org.springframework.stereotype.Component

@Component
class RaiderPvpBracketMapper {
    fun toEntity(request: CreateRaiderPvpBracketRequest): RaiderPvpBracketEntity {
        return RaiderPvpBracketEntity(
            id = null,
            raiderId = 0L, // System populated
            bracket = "", // System populated
            rating = 0, // System populated
            seasonPlayed = 0, // System populated
            weekPlayed = 0, // System populated
            maxRating = 0, // System populated
        )
    }

    fun updateEntity(
        entity: RaiderPvpBracketEntity,
        request: UpdateRaiderPvpBracketRequest,
    ): RaiderPvpBracketEntity {
        return entity.copy()
    }

    fun toResponse(entity: RaiderPvpBracketEntity): RaiderPvpBracketResponse {
        return RaiderPvpBracketResponse(
            id = entity.id!!,
        )
    }
}
