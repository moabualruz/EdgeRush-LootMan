package com.edgerush.datasync.api.wowaudit

import com.fasterxml.jackson.annotation.JsonProperty

data class CharactersResponse(
    val characters: List<CharacterDto>
)

data class CharacterDto(
    val name: String,
    val realm: String,
    val region: String,
    @JsonProperty("class")
    val clazz: String,
    val spec: String,
    val role: String,
    val gear: GearDto,
    val statistics: StatisticsDto,
    val collectibles: CollectiblesDto,
    val timestamps: TimestampDto
)

data class GearDto(
    val equipped: EquippedGearDto,
    val best: BestGearDto?,
    val spark: SparkGearDto?
)

data class EquippedGearDto(
    val head: GearItemDto?,
    val neck: GearItemDto?,
    val shoulder: GearItemDto?,
    val back: GearItemDto?,
    val chest: GearItemDto?,
    val wrist: GearItemDto?,
    val hands: GearItemDto?,
    val waist: GearItemDto?,
    val legs: GearItemDto?,
    val feet: GearItemDto?,
    @JsonProperty("finger_1")
    val finger1: GearItemDto?,
    @JsonProperty("finger_2")
    val finger2: GearItemDto?,
    @JsonProperty("trinket_1")
    val trinket1: GearItemDto?,
    @JsonProperty("trinket_2")
    val trinket2: GearItemDto?,
    @JsonProperty("main_hand")
    val mainHand: GearItemDto?,
    @JsonProperty("off_hand")
    val offHand: GearItemDto?
)

data class BestGearDto(
    val head: GearItemDto?,
    val neck: GearItemDto?,
    val shoulder: GearItemDto?,
    val back: GearItemDto?,
    val chest: GearItemDto?,
    val wrist: GearItemDto?,
    val hands: GearItemDto?,
    val waist: GearItemDto?,
    val legs: GearItemDto?,
    val feet: GearItemDto?,
    @JsonProperty("finger_1")
    val finger1: GearItemDto?,
    @JsonProperty("finger_2")
    val finger2: GearItemDto?,
    @JsonProperty("trinket_1")
    val trinket1: GearItemDto?,
    @JsonProperty("trinket_2")
    val trinket2: GearItemDto?,
    @JsonProperty("main_hand")
    val mainHand: GearItemDto?,
    @JsonProperty("off_hand")
    val offHand: GearItemDto?
)

data class SparkGearDto(
    val head: GearItemDto?,
    val neck: GearItemDto?,
    val shoulder: GearItemDto?,
    val back: GearItemDto?,
    val chest: GearItemDto?,
    val wrist: GearItemDto?,
    val hands: GearItemDto?,
    val waist: GearItemDto?,
    val legs: GearItemDto?,
    val feet: GearItemDto?,
    @JsonProperty("finger_1")
    val finger1: GearItemDto?,
    @JsonProperty("finger_2")
    val finger2: GearItemDto?,
    @JsonProperty("trinket_1")
    val trinket1: GearItemDto?,
    @JsonProperty("trinket_2")
    val trinket2: GearItemDto?,
    @JsonProperty("main_hand")
    val mainHand: GearItemDto?,
    @JsonProperty("off_hand")
    val offHand: GearItemDto?
)

data class GearItemDto(
    @JsonProperty("item_id")
    val itemId: Long?,
    @JsonProperty("item_level")
    val itemLevel: Int?,
    val quality: Int?,
    val enchant: String?,
    @JsonProperty("enchant_quality")
    val enchantQuality: Int?,
    @JsonProperty("upgrade_level")
    val upgradeLevel: Int?,
    val sockets: Int?,
    val name: String?
)

data class StatisticsDto(
    @JsonProperty("wcl")
    val warcraftLogs: WarcraftLogsDto?,
    @JsonProperty("mplus_score")
    val mythicPlusScore: Double?,
    @JsonProperty("weekly_highest_mplus")
    val weeklyHighestMPlus: Int?,
    @JsonProperty("season_highest_mplus")
    val seasonHighestMPlus: Int?,
    @JsonProperty("track_items")
    val trackItems: TrackItemsDto?,
    @JsonProperty("crest_counts")
    val crestCounts: CrestCountsDto?,
    @JsonProperty("vault_slots")
    val vaultSlots: VaultSlotsDto?,
    val renown: RenownDto?,
    val pvp: PvpDto?,
    val worldQuests: WorldQuestStatsDto?,
    val raidProgress: RaidProgressDto?
)

data class WarcraftLogsDto(
    @JsonProperty("raid_finder")
    val raidFinder: Int?,
    val normal: Int?,
    val heroic: Int?,
    val mythic: Int?
)

data class TrackItemsDto(
    val mythic: Int?,
    val heroic: Int?,
    val normal: Int?,
    @JsonProperty("raid_finder")
    val raidFinder: Int?
)

data class CrestCountsDto(
    val runed: Int?,
    val carved: Int?,
    val gilded: Int?,
    val weathered: Int?
)

data class VaultSlotsDto(
    @JsonProperty("slot_1")
    val slot1: Boolean?,
    @JsonProperty("slot_2")
    val slot2: Boolean?,
    @JsonProperty("slot_3")
    val slot3: Boolean?
)

data class RenownDto(
    @JsonProperty("assembly_of_the_deeps")
    val assemblyOfTheDeeps: Int?,
    @JsonProperty("council_of_dornogal")
    val councilOfDornogal: Int?,
    @JsonProperty("hallowfall_arathi")
    val hallowfallArathi: Int?,
    @JsonProperty("severed_threads")
    val severedThreads: Int?,
    @JsonProperty("the_karesh_trust")
    val theKareshTrust: Int?
)

data class PvpDto(
    @JsonProperty("honor_level")
    val honorLevel: Int?,
    val shuffle: ShuffleStatsDto?,
    @JsonProperty("two_v_two")
    val twoVTwo: BracketStatsDto?,
    @JsonProperty("three_v_three")
    val threeVThree: BracketStatsDto?,
    @JsonProperty("rbg")
    val ratedBattlegrounds: BracketStatsDto?
)

data class ShuffleStatsDto(
    val rating: Int?,
    @JsonProperty("season_played")
    val seasonPlayed: Int?,
    @JsonProperty("week_played")
    val weekPlayed: Int?
)

data class BracketStatsDto(
    val rating: Int?,
    @JsonProperty("season_played")
    val seasonPlayed: Int?,
    @JsonProperty("week_played")
    val weekPlayed: Int?,
    @JsonProperty("max_rating")
    val maxRating: Int?
)

data class WorldQuestStatsDto(
    @JsonProperty("done_total")
    val doneTotal: Int?,
    @JsonProperty("this_week")
    val thisWeek: Int?
)

data class RaidProgressDto(
    @JsonProperty("nerub_ar_palace")
    val nerubArPalace: RaidDifficultyDto?,
    @JsonProperty("liberation_of_undermine")
    val liberationOfUndermine: RaidDifficultyDto?,
    @JsonProperty("manaforge_omega")
    val manaforgeOmega: RaidDifficultyDto?
)

data class RaidDifficultyDto(
    val lfr: Int?,
    val normal: Int?,
    val heroic: Int?,
    val mythic: Int?
)

data class CollectiblesDto(
    val mounts: Int?,
    val toys: Int?,
    @JsonProperty("unique_pets")
    val uniquePets: Int?,
    @JsonProperty("lvl_25_pets")
    val level25Pets: Int?
)

data class TimestampDto(
    @JsonProperty("join_date")
    val joinDate: String?,
    @JsonProperty("blizzard_last_modified")
    val blizzardLastModified: String?
)
