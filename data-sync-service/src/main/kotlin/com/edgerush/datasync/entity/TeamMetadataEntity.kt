package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("team_metadata")
data class TeamMetadataEntity(
    @Id
    @Column("team_id")
    val teamId: Long,
    @Column("guild_id")
    val guildId: Long?,
    @Column("guild_name")
    val guildName: String?,
    @Column("name")
    val name: String?,
    @Column("region")
    val region: String?,
    @Column("realm")
    val realm: String?,
    @Column("url")
    val url: String?,
    @Column("last_refreshed_blizzard")
    val lastRefreshedBlizzard: OffsetDateTime?,
    @Column("last_refreshed_percentiles")
    val lastRefreshedPercentiles: OffsetDateTime?,
    @Column("last_refreshed_mythic_plus")
    val lastRefreshedMythicPlus: OffsetDateTime?,
    @Column("wishlist_updated_at")
    val wishlistUpdatedAt: OffsetDateTime?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime,
)
