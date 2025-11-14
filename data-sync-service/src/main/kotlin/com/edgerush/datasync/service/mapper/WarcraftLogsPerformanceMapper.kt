package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsPerformanceResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsPerformanceEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WarcraftLogsPerformanceMapper {
    fun toEntity(request: CreateWarcraftLogsPerformanceRequest): WarcraftLogsPerformanceEntity {
        return WarcraftLogsPerformanceEntity(
            id = null,
            fightId = request.fightId ?: 0L,
            characterName = request.characterName ?: "",
            characterRealm = request.characterRealm ?: "",
            characterClass = request.characterClass ?: "",
            characterSpec = request.characterSpec ?: "",
            deaths = request.deaths ?: 0,
            damageTaken = request.damageTaken ?: 0L,
            avoidableDamageTaken = request.avoidableDamageTaken ?: 0L,
            avoidableDamagePercentage = request.avoidableDamagePercentage ?: 0.0,
            performancePercentile = request.performancePercentile,
            itemLevel = request.itemLevel ?: 0,
            calculatedAt = request.calculatedAt ?: Instant.now(),
        )
    }

    fun updateEntity(
        entity: WarcraftLogsPerformanceEntity,
        request: UpdateWarcraftLogsPerformanceRequest,
    ): WarcraftLogsPerformanceEntity {
        return entity.copy(
            fightId = request.fightId ?: entity.fightId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            characterClass = request.characterClass ?: entity.characterClass,
            characterSpec = request.characterSpec ?: entity.characterSpec,
            deaths = request.deaths ?: entity.deaths,
            damageTaken = request.damageTaken ?: entity.damageTaken,
            avoidableDamageTaken = request.avoidableDamageTaken ?: entity.avoidableDamageTaken,
            avoidableDamagePercentage = request.avoidableDamagePercentage ?: entity.avoidableDamagePercentage,
            performancePercentile = request.performancePercentile ?: entity.performancePercentile,
            itemLevel = request.itemLevel ?: entity.itemLevel,
            calculatedAt = request.calculatedAt ?: entity.calculatedAt,
        )
    }

    fun toResponse(entity: WarcraftLogsPerformanceEntity): WarcraftLogsPerformanceResponse {
        return WarcraftLogsPerformanceResponse(
            id = entity.id!!,
            fightId = entity.fightId,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm,
            characterClass = entity.characterClass,
            characterSpec = entity.characterSpec,
            deaths = entity.deaths,
            damageTaken = entity.damageTaken,
            avoidableDamageTaken = entity.avoidableDamageTaken,
            avoidableDamagePercentage = entity.avoidableDamagePercentage,
            performancePercentile = entity.performancePercentile!!,
            itemLevel = entity.itemLevel,
            calculatedAt = entity.calculatedAt,
        )
    }
}
