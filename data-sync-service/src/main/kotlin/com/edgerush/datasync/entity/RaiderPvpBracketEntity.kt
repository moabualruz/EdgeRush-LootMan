package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_pvp_bracket_stats")
data class RaiderPvpBracketEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    val bracket: String,
    val rating: Int?,
    @Column("season_played")
    val seasonPlayed: Int?,
    @Column("week_played")
    val weekPlayed: Int?,
    @Column("max_rating")
    val maxRating: Int?,
)
