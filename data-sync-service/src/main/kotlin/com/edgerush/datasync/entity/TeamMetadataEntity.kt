package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("team_metadata")
data class TeamMetadataEntity(
    @Id
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
