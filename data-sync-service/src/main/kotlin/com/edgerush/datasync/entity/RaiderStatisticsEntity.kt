package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_statistics")
data class RaiderStatisticsEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("mythic_plus_score")
    val mythicPlusScore: Double?,
    @Column("weekly_highest_mplus")
    val weeklyHighestMplus: Int?,
    @Column("season_highest_mplus")
    val seasonHighestMplus: Int?,
    @Column("world_quests_total")
    val worldQuestsTotal: Int?,
    @Column("world_quests_this_week")
    val worldQuestsThisWeek: Int?,
    @Column("collectibles_mounts")
    val collectiblesMounts: Int?,
    @Column("collectibles_toys")
    val collectiblesToys: Int?,
    @Column("collectibles_unique_pets")
    val collectiblesUniquePets: Int?,
    @Column("collectibles_level_25_pets")
    val collectiblesLevel25Pets: Int?,
    @Column("honor_level")
    val honorLevel: Int?,
)
