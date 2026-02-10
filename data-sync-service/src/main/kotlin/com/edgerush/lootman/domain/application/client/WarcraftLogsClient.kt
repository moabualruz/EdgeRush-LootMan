package com.edgerush.lootman.domain.application.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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

    private val webClient: WebClient =
        webClientBuilder
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
    fun fetchCharacterParses(
        region: String,
        serverName: String,
        characterName: String,
    ): Mono<WarcraftLogsParseResult?> {
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
                            encounterParses =
                                char.zoneRankings?.rankings?.map { ranking ->
                                    EncounterParse(
                                        encounterName = ranking.encounter?.name ?: "Unknown",
                                        rankPercent = ranking.rankPercent ?: 0.0,
                                    )
                                } ?: emptyList(),
                        ),
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
                    log.debug(
                        "Successfully fetched Warcraft Logs parses for {}: best avg = {}",
                        result.characterName,
                        result.bestPerformanceAverage,
                    )
                } else {
                    log.debug("No Warcraft Logs data found for {}-{}-{}", characterName, normalizedServer, normalizedRegion)
                }
            }
            .doOnError { error ->
                log.warn(
                    "Failed to fetch Warcraft Logs parses for {}-{}-{}: {}",
                    characterName,
                    normalizedServer,
                    normalizedRegion,
                    error.message,
                )
            }
    }

    /**
     * Fetches guild reports from Warcraft Logs with fight-level performance data.
     *
     * Returns report metadata, fights, and per-fight player performance (deaths, DPS/HPS, parse %).
     * Used to populate the warcraft_logs_reports/fights/performance tables for MAS calculation.
     *
     * @param guildName The guild name (as it appears on WCL)
     * @param serverSlug The server slug (lowercase, hyphenated)
     * @param serverRegion The region (us, eu, kr, tw, cn)
     * @param limit Maximum number of reports to fetch (default 10)
     * @return List of guild report data, or empty list on error
     */
    fun fetchGuildReports(
        guildName: String,
        serverSlug: String,
        serverRegion: String,
        limit: Int = 10,
    ): Mono<List<GuildReportData>> {
        val normalizedServer = normalizeServerSlug(serverSlug)
        val normalizedRegion = serverRegion.lowercase()

        val query = buildGuildReportsQuery(guildName, normalizedServer, normalizedRegion, limit)

        return webClient
            .post()
            .uri("/api/v2/client")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
            .bodyValue(query)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Guild not found")
                    .flatMap { Mono.error(WarcraftLogsNotFoundException("Guild not found: $guildName-$normalizedServer-$normalizedRegion")) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("Warcraft Logs server error")
                    .flatMap { Mono.error(WarcraftLogsServerException(it)) }
            }
            .bodyToMono(GuildReportsGraphQLResponse::class.java)
            .timeout(Duration.ofSeconds(60))
            .map { response ->
                val reports = response.data?.reportData?.reports?.data ?: emptyList()
                reports.map { report ->
                    val fights = report.fights?.filter { it.encounterID != null && it.encounterID > 0 } ?: emptyList()
                    GuildReportData(
                        reportCode = report.code ?: "",
                        title = report.title ?: "",
                        owner = report.owner?.name ?: "",
                        startTime = report.startTime ?: 0L,
                        endTime = report.endTime ?: 0L,
                        zone = report.zone?.name,
                        fights = fights.map { fight ->
                            GuildReportFight(
                                fightId = fight.id ?: 0,
                                encounterId = fight.encounterID ?: 0,
                                encounterName = fight.name ?: "Unknown",
                                difficulty = fight.difficulty ?: 0,
                                kill = fight.kill ?: false,
                                startTimeOffset = fight.startTime ?: 0L,
                                endTimeOffset = fight.endTime ?: 0L,
                                bossPercentage = fight.bossPercentage ?: 0.0,
                                playerDetails = emptyList(), // WCL fights API returns friendlyPlayers (IDs only), not player details
                            )
                        },
                    )
                }
            }
            .doOnSubscribe {
                log.debug("Fetching guild reports for {}-{}-{}", guildName, normalizedServer, normalizedRegion)
            }
            .doOnSuccess { reports ->
                log.debug("Fetched {} guild reports for {}", reports.size, guildName)
            }
            .doOnError { error ->
                log.warn("Failed to fetch guild reports for {}-{}-{}: {}", guildName, normalizedServer, normalizedRegion, error.message)
            }
            .onErrorResume { e ->
                log.warn("Returning empty reports due to error: {}", e.message)
                Mono.just(emptyList())
            }
    }

    private fun buildGraphQLQuery(
        characterName: String,
        serverSlug: String,
        serverRegion: String,
    ): Map<String, Any> {
        val query =
            """
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
            "variables" to
                mapOf(
                    "name" to characterName,
                    "serverSlug" to serverSlug,
                    "serverRegion" to serverRegion,
                ),
        )
    }

    private fun buildGuildReportsQuery(
        guildName: String,
        serverSlug: String,
        serverRegion: String,
        limit: Int,
    ): Map<String, Any> {
        val query =
            """
            query GuildReports(${'$'}guildName: String!, ${'$'}serverSlug: String!, ${'$'}serverRegion: String!, ${'$'}limit: Int!) {
                reportData {
                    reports(guildName: ${'$'}guildName, guildServerSlug: ${'$'}serverSlug, guildServerRegion: ${'$'}serverRegion, limit: ${'$'}limit) {
                        data {
                            code
                            title
                            startTime
                            endTime
                            owner { name }
                            zone { name }
                            fights(killType: Encounters) {
                                id
                                encounterID
                                name
                                difficulty
                                kill
                                startTime
                                endTime
                                bossPercentage
                                friendlyPlayers
                            }
                        }
                    }
                }
            }
            """.trimIndent()

        return mapOf(
            "query" to query,
            "variables" to
                mapOf(
                    "guildName" to guildName,
                    "serverSlug" to serverSlug,
                    "serverRegion" to serverRegion,
                    "limit" to limit,
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

// ============================================================================
// Guild Report Data Models
// ============================================================================

/**
 * Parsed guild report data from Warcraft Logs.
 */
data class GuildReportData(
    val reportCode: String,
    val title: String,
    val owner: String,
    val startTime: Long,
    val endTime: Long,
    val zone: String?,
    val fights: List<GuildReportFight>,
)

/**
 * A single fight (boss encounter) within a report.
 */
data class GuildReportFight(
    val fightId: Int,
    val encounterId: Int,
    val encounterName: String,
    val difficulty: Int,
    val kill: Boolean,
    val startTimeOffset: Long,
    val endTimeOffset: Long,
    val bossPercentage: Double,
    val playerDetails: List<GuildReportPlayerPerformance>,
)

/**
 * Performance data for a single player in a fight.
 */
data class GuildReportPlayerPerformance(
    val name: String,
    val server: String,
    val type: String,
    val spec: String,
    val deaths: Int,
)

// ============================================================================
// GraphQL Response DTOs — Character Parses
// ============================================================================

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

// ============================================================================
// GraphQL Response DTOs — Guild Reports
// ============================================================================

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportsGraphQLResponse(
    val data: GuildReportsData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportsData(
    val reportData: GuildReportsReportData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportsReportData(
    val reports: GuildReportsPage?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportsPage(
    val data: List<GuildReportNode>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportNode(
    val code: String?,
    val title: String?,
    val startTime: Long?,
    val endTime: Long?,
    val owner: GuildReportOwner?,
    val zone: GuildReportZone?,
    val fights: List<GuildReportFightNode>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportOwner(
    val name: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportZone(
    val name: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportFightNode(
    val id: Int?,
    val encounterID: Int?,
    val name: String?,
    val difficulty: Int?,
    val kill: Boolean?,
    val startTime: Long?,
    val endTime: Long?,
    val bossPercentage: Double?,
    val friendlyPlayers: List<Int>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GuildReportPlayerDetail(
    val name: String?,
    val server: String?,
    val type: String?,
    val spec: String?,
)

class WarcraftLogsNotFoundException(message: String) : RuntimeException(message)

class WarcraftLogsServerException(message: String) : RuntimeException(message)
