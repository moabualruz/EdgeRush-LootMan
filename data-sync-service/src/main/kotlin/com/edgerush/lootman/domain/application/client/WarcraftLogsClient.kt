package com.edgerush.lootman.domain.application.client

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsProperties
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Client for the Warcraft Logs GraphQL API.
 *
 * Fetches character parse data for recruitment applications.
 * Uses OAuth2 client credentials flow for authentication.
 *
 * @see <a href="https://www.warcraftlogs.com/api/docs">Warcraft Logs API Documentation</a>
 */
@Component
class WarcraftLogsClient(
    webClientBuilder: WebClient.Builder,
    private val tokenProvider: WarcraftLogsTokenProvider,
    baseUrl: String = DEFAULT_BASE_URL,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient = webClientBuilder
        .baseUrl(baseUrl)
        .build()

    /**
     * Fetches character parse data from Warcraft Logs.
     *
     * @param region The region (us, eu, kr, tw, cn)
     * @param serverName The server/realm name
     * @param characterName The character's name
     * @return Character parse data or null if not found
     */
    fun fetchCharacterParses(region: String, serverName: String, characterName: String): Mono<WarcraftLogsParseResult?> {
        val normalizedServer = normalizeServerSlug(serverName)
        val normalizedRegion = region.lowercase()

        val query = buildGraphQLQuery(characterName, normalizedServer, normalizedRegion)

        return webClient
            .post()
            .uri("/api/v2/client")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
            .bodyValue(query)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Character not found")
                    .flatMap { Mono.error(WarcraftLogsNotFoundException("Character not found: $characterName-$serverName-$region")) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Warcraft Logs server error")
                    .flatMap { Mono.error(WarcraftLogsServerException(it)) }
            }
            .bodyToMono(WarcraftLogsGraphQLResponse::class.java)
            .timeout(Duration.ofSeconds(30))
            .flatMap { response ->
                val char = response.data?.characterData?.character
                if (char != null) {
                    Mono.just(
                        WarcraftLogsParseResult(
                            characterName = char.name,
                            serverName = char.server?.name ?: serverName,
                            region = char.server?.region?.slug ?: normalizedRegion,
                            bestPerformanceAverage = char.zoneRankings?.bestPerformanceAverage,
                            medianPerformanceAverage = char.zoneRankings?.medianPerformanceAverage,
                            encounterParses = char.zoneRankings?.rankings?.map { ranking ->
                                EncounterParse(
                                    encounterName = ranking.encounter?.name ?: "Unknown",
                                    rankPercent = ranking.rankPercent ?: 0.0,
                                )
                            } ?: emptyList(),
                        )
                    )
                } else {
                    Mono.empty()
                }
            }
            .doOnSubscribe {
                log.debug("Fetching Warcraft Logs parses for {}-{}-{}", characterName, normalizedServer, normalizedRegion)
            }
            .doOnSuccess { result ->
                if (result != null) {
                    log.debug("Successfully fetched Warcraft Logs parses for {}: best avg = {}", result.characterName, result.bestPerformanceAverage)
                } else {
                    log.debug("No Warcraft Logs data found for {}-{}-{}", characterName, normalizedServer, normalizedRegion)
                }
            }
            .doOnError { error ->
                log.warn("Failed to fetch Warcraft Logs parses for {}-{}-{}: {}", characterName, normalizedServer, normalizedRegion, error.message)
            }
    }

    private fun buildGraphQLQuery(characterName: String, serverSlug: String, serverRegion: String): Map<String, Any> {
        val query = """
            query CharacterParses(${'$'}name: String!, ${'$'}serverSlug: String!, ${'$'}serverRegion: String!) {
                characterData {
                    character(name: ${'$'}name, serverSlug: ${'$'}serverSlug, serverRegion: ${'$'}serverRegion) {
                        name
                        server {
                            name
                            region { slug }
                        }
                        zoneRankings(difficulty: 5) {
                            bestPerformanceAverage
                            medianPerformanceAverage
                            rankings {
                                encounter { name }
                                rankPercent
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        return mapOf(
            "query" to query,
            "variables" to mapOf(
                "name" to characterName,
                "serverSlug" to serverSlug,
                "serverRegion" to serverRegion,
            ),
        )
    }

    /**
     * Normalizes server name to slug format (lowercase, hyphenated).
     */
    private fun normalizeServerSlug(serverName: String): String {
        return serverName
            .lowercase()
            .replace(" ", "-")
            .replace("'", "")
    }

    companion object {
        private const val DEFAULT_BASE_URL = "https://www.warcraftlogs.com"
    }
}

/**
 * Result of fetching character parses from Warcraft Logs.
 */
data class WarcraftLogsParseResult(
    val characterName: String,
    val serverName: String,
    val region: String,
    val bestPerformanceAverage: Double?,
    val medianPerformanceAverage: Double?,
    val encounterParses: List<EncounterParse>,
)

/**
 * Parse data for a single encounter.
 */
data class EncounterParse(
    val encounterName: String,
    val rankPercent: Double,
)

// GraphQL Response DTOs

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsGraphQLResponse(
    val data: WarcraftLogsData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsData(
    val characterData: WarcraftLogsCharacterData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsCharacterData(
    val character: WarcraftLogsCharacter?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsCharacter(
    val name: String,
    val server: WarcraftLogsServer?,
    val zoneRankings: WarcraftLogsZoneRankings?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsServer(
    val name: String?,
    val region: WarcraftLogsRegion?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsRegion(
    val slug: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsZoneRankings(
    val bestPerformanceAverage: Double?,
    val medianPerformanceAverage: Double?,
    val rankings: List<WarcraftLogsRanking>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsRanking(
    val encounter: WarcraftLogsEncounter?,
    val rankPercent: Double?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WarcraftLogsEncounter(
    val name: String?,
)

class WarcraftLogsNotFoundException(message: String) : RuntimeException(message)
class WarcraftLogsServerException(message: String) : RuntimeException(message)
