package com.edgerush.lootman.domain.application.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Client for the Raider.IO API.
 *
 * The Raider.IO API is public and doesn't require authentication for basic character data.
 * Base URL: https://raider.io/api/v1
 *
 * @see <a href="https://raider.io/api">Raider.IO API Documentation</a>
 */
@Component
class RaiderIOClient(
    webClientBuilder: WebClient.Builder,
    baseUrl: String = BASE_URL,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(baseUrl)
            .build()

    /**
     * Fetches character profile data from Raider.IO.
     *
     * @param region The region (us, eu, kr, tw, cn)
     * @param realm The character's realm (lowercase, hyphenated)
     * @param name The character's name
     * @return Character profile data as JSON
     */
    fun fetchCharacterProfile(
        region: String,
        realm: String,
        name: String,
    ): Mono<RaiderIOCharacterProfile> {
        val normalizedRealm = normalizeRealm(realm)

        return webClient
            .get()
            .uri { builder ->
                builder
                    .path("/characters/profile")
                    .queryParam("region", region.lowercase())
                    .queryParam("realm", normalizedRealm)
                    .queryParam("name", name)
                    .queryParam("fields", FIELDS)
                    .build()
            }
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Character not found")
                    .flatMap { Mono.error(RaiderIONotFoundException("Character not found: $name-$realm-$region")) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Raider.IO server error")
                    .flatMap { Mono.error(RaiderIOServerException(it)) }
            }
            .bodyToMono(RaiderIOCharacterProfile::class.java)
            .timeout(Duration.ofSeconds(30))
            .doOnSubscribe {
                log.debug("Fetching Raider.IO profile for {}-{}-{}", name, normalizedRealm, region)
            }
            .doOnSuccess {
                log.debug("Successfully fetched Raider.IO profile for {}", it.name)
            }
            .doOnError { error ->
                log.warn("Failed to fetch Raider.IO profile for {}-{}-{}: {}", name, normalizedRealm, region, error.message)
            }
    }

    /**
     * Normalizes realm name to Raider.IO format (lowercase, hyphenated).
     */
    private fun normalizeRealm(realm: String): String {
        return realm
            .lowercase()
            .replace(" ", "-")
            .replace("'", "")
    }

    companion object {
        private const val BASE_URL = "https://raider.io/api/v1"
        private const val FIELDS = "mythic_plus_scores_by_season:current,raid_progression,gear"
    }
}

/**
 * Character profile data from Raider.IO.
 */
data class RaiderIOCharacterProfile(
    val name: String,
    val race: String,
    @JsonProperty("class")
    val characterClass: String,
    @JsonProperty("active_spec_name")
    val activeSpecName: String?,
    @JsonProperty("active_spec_role")
    val activeSpecRole: String?,
    val gender: String,
    val faction: String,
    val region: String,
    val realm: String,
    @JsonProperty("profile_url")
    val profileUrl: String,
    val gear: RaiderIOGear?,
    @JsonProperty("mythic_plus_scores_by_season")
    val mythicPlusScoresBySeason: List<RaiderIOMythicPlusSeasonScore>?,
    @JsonProperty("raid_progression")
    val raidProgression: Map<String, RaiderIORaidProgression>?,
) {
    /**
     * Gets the current season's Mythic+ score.
     */
    fun getCurrentMythicPlusScore(): Double? {
        return mythicPlusScoresBySeason?.firstOrNull()?.scores?.all
    }

    /**
     * Gets the item level from gear.
     */
    fun getItemLevel(): Double? {
        return gear?.itemLevelEquipped
    }
}

data class RaiderIOGear(
    @JsonProperty("item_level_equipped")
    val itemLevelEquipped: Double?,
    @JsonProperty("item_level_total")
    val itemLevelTotal: Double?,
    val items: Map<String, RaiderIOGearItem?>?,
)

data class RaiderIOGearItem(
    @JsonProperty("item_id")
    val itemId: Long?,
    @JsonProperty("item_level")
    val itemLevel: Int?,
    @JsonProperty("item_quality")
    val itemQuality: Int?,
    val name: String?,
    val icon: String?,
    @JsonProperty("is_legendary")
    val isLegendary: Boolean?,
    val enchants: List<Int?>?,
    val gems: List<Int?>?,
    val bonuses: List<Int?>?,
    val tier: String?,
)

data class RaiderIOMythicPlusSeasonScore(
    val season: String,
    val scores: RaiderIOScores,
)

data class RaiderIOScores(
    val all: Double,
    val dps: Double,
    val healer: Double,
    val tank: Double,
    val spec0: Double?,
    val spec1: Double?,
    val spec2: Double?,
    val spec3: Double?,
)

data class RaiderIORaidProgression(
    val summary: String,
    @JsonProperty("total_bosses")
    val totalBosses: Int,
    @JsonProperty("normal_bosses_killed")
    val normalBossesKilled: Int,
    @JsonProperty("heroic_bosses_killed")
    val heroicBossesKilled: Int,
    @JsonProperty("mythic_bosses_killed")
    val mythicBossesKilled: Int,
)

class RaiderIONotFoundException(message: String) : RuntimeException(message)

class RaiderIOServerException(message: String) : RuntimeException(message)
