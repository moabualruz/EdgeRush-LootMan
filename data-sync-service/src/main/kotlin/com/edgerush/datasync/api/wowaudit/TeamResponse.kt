package com.edgerush.datasync.api.wowaudit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamResponse(
    val id: Long,
    @JsonProperty("guild_id")
    val guildId: Long,
    val name: String? = null,
    val region: String? = null,
    val realm: String? = null
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
