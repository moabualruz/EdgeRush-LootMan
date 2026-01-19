package com.edgerush.datasync.client

import com.edgerush.datasync.config.SyncProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Client for WoWAudit API.
 *
 * Provides methods to fetch guild data including roster, attendance, loot history,
 * raids, and applications from WoWAudit.
 */
@Component
class WoWAuditClient(
    @Qualifier("wowauditWebClient") private val webClient: WebClient,
    private val properties: SyncProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Fetches the guild roster (all characters).
     */
    fun fetchRoster(): Mono<String> = get("/v1/characters")

    /**
     * Fetches loot history for a specific season.
     */
    fun fetchLootHistory(seasonId: Long): Mono<String> = get("/v1/loot_history/$seasonId")

    /**
     * Fetches all wishlists.
     */
    fun fetchWishlists(): Mono<String> = get("/v1/wishlists")

    /**
     * Fetches a specific wishlist by ID.
     */
    fun fetchWishlistDetail(id: Long): Mono<String> = get("/v1/wishlists/$id")

    /**
     * Fetches team information.
     */
    fun fetchTeam(): Mono<String> = get("/v1/team")

    /**
     * Fetches the current period information.
     */
    fun fetchPeriod(): Mono<String> = get("/v1/period")

    /**
     * Fetches attendance data.
     */
    fun fetchAttendance(): Mono<String> = get("/v1/attendance")

    /**
     * Fetches raids, optionally including past raids.
     */
    fun fetchRaids(includePast: Boolean = true): Mono<String> =
        get(if (includePast) "/v1/raids?include_past=true" else "/v1/raids")

    /**
     * Fetches a specific raid by ID.
     */
    fun fetchRaidDetail(id: Long): Mono<String> = get("/v1/raids/$id")

    /**
     * Fetches historical data for a specific period.
     */
    fun fetchHistoricalData(periodId: Long): Mono<String> = get("/v1/historical_data?period=$periodId")

    /**
     * Fetches history for a specific character.
     */
    fun fetchCharacterHistory(characterId: Long): Mono<String> = get("/v1/historical_data/$characterId")

    /**
     * Fetches guest information.
     */
    fun fetchGuests(): Mono<String> = get("/v1/guests")

    /**
     * Fetches all applications.
     */
    fun fetchApplications(): Mono<String> = get("/v1/applications")

    /**
     * Fetches a specific application by ID.
     */
    fun fetchApplicationDetail(id: Long): Mono<String> = get("/v1/applications/$id")

    private fun get(path: String): Mono<String> =
        webClient
            .get()
            .uri(path)
            .retrieve()
            .onStatus({ it == HttpStatus.TOO_MANY_REQUESTS }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("WoWAudit rate limit hit")
                    .flatMap { Mono.error(WoWAuditRateLimitException(it)) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("WoWAudit server error (${response.statusCode()})")
                    .flatMap { Mono.error(WoWAuditServerException(it)) }
            }
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("WoWAudit client error (${response.statusCode()})")
                    .flatMap { body -> Mono.error(WoWAuditClientErrorException(body)) }
            }
            .bodyToMono(String::class.java)
            .map { body ->
                val snippet = body.trim()
                if (snippet.startsWith("<")) {
                    log.warn("WoWAudit response for '{}' was not JSON. First bytes: {}", path, snippet.take(120))
                    throw WoWAuditUnexpectedResponse("Expected JSON but received HTML. Snippet: ${snippet.take(200)}")
                }
                body
            }
            .doOnSubscribe {
                require(!properties.wowaudit.guildProfileUri.isNullOrBlank()) {
                    "sync.wowaudit.guild-profile-uri must be configured"
                }
            }
}
