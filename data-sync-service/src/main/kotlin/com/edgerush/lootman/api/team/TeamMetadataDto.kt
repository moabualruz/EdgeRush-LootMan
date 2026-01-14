package com.edgerush.lootman.api.team

import com.edgerush.datasync.entity.TeamMetadataEntity
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

/**
 * Request DTO for creating team metadata.
 */
data class CreateTeamMetadataRequest(
    @field:NotNull(message = "Team ID is required")
    val teamId: Long,
    val guildId: Long? = null,
    val guildName: String? = null,
    val name: String? = null,
    val region: String? = null,
    val realm: String? = null,
    val url: String? = null,
    val lastRefreshedBlizzard: OffsetDateTime? = null,
    val lastRefreshedPercentiles: OffsetDateTime? = null,
    val lastRefreshedMythicPlus: OffsetDateTime? = null,
    val wishlistUpdatedAt: OffsetDateTime? = null,
)

/**
 * Request DTO for updating team metadata.
 */
data class UpdateTeamMetadataRequest(
    val guildId: Long? = null,
    val guildName: String? = null,
    val name: String? = null,
    val region: String? = null,
    val realm: String? = null,
    val url: String? = null,
    val lastRefreshedBlizzard: OffsetDateTime? = null,
    val lastRefreshedPercentiles: OffsetDateTime? = null,
    val lastRefreshedMythicPlus: OffsetDateTime? = null,
    val wishlistUpdatedAt: OffsetDateTime? = null,
)

/**
 * Response DTO for team metadata.
 */
data class TeamMetadataResponse(
    val teamId: Long,
    val guildId: Long?,
    val guildName: String?,
    val name: String?,
    val region: String?,
    val realm: String?,
    val url: String?,
    val lastRefreshedBlizzard: OffsetDateTime?,
    val lastRefreshedPercentiles: OffsetDateTime?,
    val lastRefreshedMythicPlus: OffsetDateTime?,
    val wishlistUpdatedAt: OffsetDateTime?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: TeamMetadataEntity): TeamMetadataResponse =
            TeamMetadataResponse(
                teamId = entity.teamId,
                guildId = entity.guildId,
                guildName = entity.guildName,
                name = entity.name,
                region = entity.region,
                realm = entity.realm,
                url = entity.url,
                lastRefreshedBlizzard = entity.lastRefreshedBlizzard,
                lastRefreshedPercentiles = entity.lastRefreshedPercentiles,
                lastRefreshedMythicPlus = entity.lastRefreshedMythicPlus,
                wishlistUpdatedAt = entity.wishlistUpdatedAt,
                syncedAt = entity.syncedAt,
            )
    }
}

/**
 * Response DTO for exists check.
 */
data class TeamMetadataExistsResponse(val exists: Boolean)

/**
 * Response DTO for count.
 */
data class TeamMetadataCountResponse(val count: Long)
