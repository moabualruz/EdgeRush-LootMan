package com.edgerush.datasync.api.wowaudit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamResponse(
    val id: Long,
    @JsonProperty("guild_id")
    val guildId: Long? = null,
    @JsonProperty("guild_name")
    val guildName: String? = null,
    val name: String? = null,
    val region: String? = null,
    val realm: String? = null,
    val url: String? = null,
    @JsonProperty("last_refreshed")
    val lastRefreshed: LastRefreshed? = null,
    @JsonProperty("raid_days")
    val raidDays: List<RaidDayResponse> = emptyList(),
    @JsonProperty("wishlist_updated_at")
    val wishlistUpdatedAt: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PeriodResponse(
    @JsonProperty("season_id")
    val seasonId: Long?,
    @JsonProperty("period_id")
    val periodId: Long? = null,
    @JsonProperty("current_period")
    val currentPeriod: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LastRefreshed(
    val blizzard: String? = null,
    val percentiles: String? = null,
    @JsonProperty("mythic_plus")
    val mythicPlus: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RaidDayResponse(
    @JsonProperty("week_day")
    val weekDay: String? = null,
    @JsonProperty("start_time")
    val startTime: String? = null,
    @JsonProperty("end_time")
    val endTime: String? = null,
    @JsonProperty("current_instance")
    val currentInstance: String? = null,
    val difficulty: String? = null,
    @JsonProperty("active_from")
    val activeFrom: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RosterCharacterResponse(
    val id: Long? = null,
    val name: String,
    val realm: String? = null,
    val role: String? = null,
    @JsonProperty("class")
    val clazz: String? = null,
    val rank: String? = null,
    val status: String? = null,
    @JsonProperty("tracking_since")
    val trackingSince: String? = null,
    val region: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LootHistoryEntryResponse(
    val character: LootCharacterNode? = null,
    val item: LootItemNode? = null,
    val tier: String? = null,
    @JsonProperty("awarded_at")
    val awardedAt: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LootCharacterNode(
    val name: String? = null,
    val realm: String? = null,
    val role: String? = null,
    @JsonProperty("class")
    val clazz: String? = null,
    val region: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LootItemNode(
    val id: Long? = null,
    val name: String? = null
)
