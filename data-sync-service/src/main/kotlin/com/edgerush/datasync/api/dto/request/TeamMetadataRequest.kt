package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreateTeamMetadataRequest(
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
    val syncedAt: OffsetDateTime? = null,
)

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
    val syncedAt: OffsetDateTime? = null,
)
