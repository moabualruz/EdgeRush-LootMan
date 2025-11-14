package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.response.RaiderStatisticsResponse
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import org.springframework.stereotype.Component

@Component
class RaiderStatisticsMapper {
    fun toEntity(request: CreateRaiderStatisticsRequest): RaiderStatisticsEntity {
        return RaiderStatisticsEntity(
            id = null,
            raiderId = 0L, // System populated
            mythicPlusScore = 0.0, // System populated
            weeklyHighestMplus = 0, // System populated
            seasonHighestMplus = 0, // System populated
            worldQuestsTotal = 0, // System populated
            worldQuestsThisWeek = 0, // System populated
            collectiblesMounts = 0, // System populated
            collectiblesToys = 0, // System populated
            collectiblesUniquePets = 0, // System populated
            collectiblesLevel25Pets = 0, // System populated
            honorLevel = 0, // System populated
        )
    }

    fun updateEntity(
        entity: RaiderStatisticsEntity,
        request: UpdateRaiderStatisticsRequest,
    ): RaiderStatisticsEntity {
        return entity.copy()
    }

    fun toResponse(entity: RaiderStatisticsEntity): RaiderStatisticsResponse {
        return RaiderStatisticsResponse(
            id = entity.id!!,
        )
    }
}
