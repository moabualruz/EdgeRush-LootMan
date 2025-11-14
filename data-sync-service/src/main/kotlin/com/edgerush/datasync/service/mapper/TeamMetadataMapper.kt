package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateTeamMetadataRequest
import com.edgerush.datasync.api.dto.request.UpdateTeamMetadataRequest
import com.edgerush.datasync.api.dto.response.TeamMetadataResponse
import com.edgerush.datasync.entity.TeamMetadataEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class TeamMetadataMapper {

    fun toEntity(request: CreateTeamMetadataRequest): TeamMetadataEntity {
        return TeamMetadataEntity(
            teamId = 0L, // TODO: Set from request or context
            guildId = request.guildId,
            guildName = request.guildName,
            name = request.name,
            region = request.region,
            realm = request.realm,
            url = request.url,
            lastRefreshedBlizzard = request.lastRefreshedBlizzard,
            lastRefreshedPercentiles = request.lastRefreshedPercentiles,
            lastRefreshedMythicPlus = request.lastRefreshedMythicPlus,
            wishlistUpdatedAt = request.wishlistUpdatedAt,
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(entity: TeamMetadataEntity, request: UpdateTeamMetadataRequest): TeamMetadataEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            guildName = request.guildName ?: entity.guildName,
            name = request.name ?: entity.name,
            region = request.region ?: entity.region,
            realm = request.realm ?: entity.realm,
            url = request.url ?: entity.url,
            lastRefreshedBlizzard = request.lastRefreshedBlizzard ?: entity.lastRefreshedBlizzard,
            lastRefreshedPercentiles = request.lastRefreshedPercentiles ?: entity.lastRefreshedPercentiles,
            lastRefreshedMythicPlus = request.lastRefreshedMythicPlus ?: entity.lastRefreshedMythicPlus,
            wishlistUpdatedAt = request.wishlistUpdatedAt ?: entity.wishlistUpdatedAt,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: TeamMetadataEntity): TeamMetadataResponse {
        return TeamMetadataResponse(
            teamId = entity.teamId,
            guildId = entity.guildId!!,
            guildName = entity.guildName!!,
            name = entity.name!!,
            region = entity.region!!,
            realm = entity.realm!!,
            url = entity.url!!,
            lastRefreshedBlizzard = entity.lastRefreshedBlizzard!!,
            lastRefreshedPercentiles = entity.lastRefreshedPercentiles!!,
            lastRefreshedMythicPlus = entity.lastRefreshedMythicPlus!!,
            wishlistUpdatedAt = entity.wishlistUpdatedAt!!,
            syncedAt = entity.syncedAt,
        )
    }
}
