package com.edgerush.datasync.service

import java.time.OffsetDateTime

data class RaiderRecord(
    val wowauditId: Long?,
    val name: String,
    val realm: String,
    val region: String,
    val clazz: String,
    val spec: String,
    val role: String,
    val rank: String?,
    val status: String?,
    val note: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val joinDate: OffsetDateTime?,
    val blizzardLastModified: OffsetDateTime?,
    val gearItems: List<RaiderGearItemRecord> = emptyList(),
    val statistics: RaiderStatisticsRecord? = null,
    val pvpBrackets: List<RaiderPvpBracketRecord> = emptyList()
)

data class RaiderGearItemRecord(
    val gearSet: String,
    val slot: String,
    val itemId: Long?,
    val itemLevel: Int?,
    val quality: Int?,
    val enchant: String?,
    val enchantQuality: Int?,
    val upgradeLevel: Int?,
    val sockets: Int?,
    val name: String?
)

data class RaiderStatisticsRecord(
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
    val warcraftLogs: List<WarcraftLogRecord> = emptyList(),
    val trackItems: List<TrackItemRecord> = emptyList(),
    val crestCounts: List<CrestCountRecord> = emptyList(),
    val vaultSlots: List<VaultSlotRecord> = emptyList(),
    val renownLevels: List<RenownRecord> = emptyList(),
    val raidProgress: List<RaidProgressRecord> = emptyList()
)

data class WarcraftLogRecord(
    val difficulty: String,
    val score: Int?
)

data class TrackItemRecord(
    val tier: String,
    val itemCount: Int?
)

data class CrestCountRecord(
    val crestType: String,
    val count: Int?
)

data class VaultSlotRecord(
    val slot: String,
    val unlocked: Boolean?
)

data class RenownRecord(
    val faction: String,
    val level: Int?
)

data class RaidProgressRecord(
    val raid: String,
    val difficulty: String,
    val bossesDefeated: Int?
)

data class RaiderPvpBracketRecord(
    val bracket: String,
    val rating: Int?,
    val seasonPlayed: Int?,
    val weekPlayed: Int?,
    val maxRating: Int?
)
