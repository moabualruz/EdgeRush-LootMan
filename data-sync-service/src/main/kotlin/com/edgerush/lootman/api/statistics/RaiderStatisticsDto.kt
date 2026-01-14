package com.edgerush.lootman.api.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import jakarta.validation.constraints.NotNull

data class CreateRaiderStatisticsRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    val mythicPlusScore: Double? = null,
    val weeklyHighestMplus: Int? = null,
    val seasonHighestMplus: Int? = null,
    val worldQuestsTotal: Int? = null,
    val worldQuestsThisWeek: Int? = null,
    val collectiblesMounts: Int? = null,
    val collectiblesToys: Int? = null,
    val collectiblesUniquePets: Int? = null,
    val collectiblesLevel25Pets: Int? = null,
    val honorLevel: Int? = null,
)

data class UpdateRaiderStatisticsRequest(
    val mythicPlusScore: Double? = null,
    val weeklyHighestMplus: Int? = null,
    val seasonHighestMplus: Int? = null,
    val worldQuestsTotal: Int? = null,
    val worldQuestsThisWeek: Int? = null,
    val collectiblesMounts: Int? = null,
    val collectiblesToys: Int? = null,
    val collectiblesUniquePets: Int? = null,
    val collectiblesLevel25Pets: Int? = null,
    val honorLevel: Int? = null,
)

data class RaiderStatisticsResponse(
    val id: Long,
    val raiderId: Long,
    val mythicPlusScore: Double?,
    val weeklyHighestMplus: Int?,
    val seasonHighestMplus: Int?,
    val worldQuestsTotal: Int?,
    val worldQuestsThisWeek: Int?,
    val collectiblesMounts: Int?,
    val collectiblesToys: Int?,
    val collectiblesUniquePets: Int?,
    val collectiblesLevel25Pets: Int?,
    val honorLevel: Int?,
) {
    companion object {
        fun from(e: RaiderStatisticsEntity) = RaiderStatisticsResponse(
            e.id!!, e.raiderId, e.mythicPlusScore, e.weeklyHighestMplus, e.seasonHighestMplus,
            e.worldQuestsTotal, e.worldQuestsThisWeek, e.collectiblesMounts, e.collectiblesToys,
            e.collectiblesUniquePets, e.collectiblesLevel25Pets, e.honorLevel
        )
    }
}

data class RaiderStatisticsExistsResponse(val exists: Boolean)
data class RaiderStatisticsCountResponse(val count: Long)
