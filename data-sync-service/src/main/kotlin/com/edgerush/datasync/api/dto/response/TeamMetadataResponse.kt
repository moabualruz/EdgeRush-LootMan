package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

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
    val syncedAt: OffsetDateTime
)
