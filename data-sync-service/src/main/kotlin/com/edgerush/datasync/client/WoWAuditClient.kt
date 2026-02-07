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
    fun fetchRoster(apiKey: String? = null): Mono<String> = get("/v1/characters", apiKey)

    /**
     * Fetches loot history for a specific season.
     */
    fun fetchLootHistory(seasonId: Long, apiKey: String? = null): Mono<String> = get("/v1/loot_history/$seasonId", apiKey)

    /**
     * Fetches all wishlists.
     */
    fun fetchWishlists(apiKey: String? = null): Mono<String> = get("/v1/wishlists", apiKey)

    /**
     * Fetches a specific wishlist by ID.
     */
    fun fetchWishlistDetail(id: Long, apiKey: String? = null): Mono<String> = get("/v1/wishlists/$id", apiKey)

    /**
     * Fetches team information.
     */
    fun fetchTeam(apiKey: String? = null): Mono<String> = get("/v1/team", apiKey)

    /**
     * Fetches the current period information.
     */
    fun fetchPeriod(apiKey: String? = null): Mono<String> = get("/v1/period", apiKey)

    /**
     * Fetches attendance data.
     */
    fun fetchAttendance(apiKey: String? = null): Mono<String> = get("/v1/attendance", apiKey)

    /**
     * Fetches raids, optionally including past raids.
     */
    fun fetchRaids(includePast: Boolean = true, apiKey: String? = null): Mono<String> =
        get(if (includePast) "/v1/raids?include_past=true" else "/v1/raids", apiKey)

    /**
     * Fetches a specific raid by ID.
     */
    fun fetchRaidDetail(id: Long, apiKey: String? = null): Mono<String> = get("/v1/raids/$id", apiKey)

    /**
     * Fetches historical data for a specific period.
     */
    fun fetchHistoricalData(periodId: Long, apiKey: String? = null): Mono<String> = get("/v1/historical_data?period=$periodId", apiKey)

    /**
     * Fetches history for a specific character.
     */
    fun fetchCharacterHistory(characterId: Long, apiKey: String? = null): Mono<String> = get("/v1/historical_data/$characterId", apiKey)

    /**
     * Fetches guest information.
     */
    fun fetchGuests(apiKey: String? = null): Mono<String> = get("/v1/guests", apiKey)

    /**
     * Fetches all applications.
     */
    fun fetchApplications(apiKey: String? = null): Mono<String> = get("/v1/applications", apiKey)

    /**
     * Fetches a specific application by ID.
     */
    fun fetchApplicationDetail(id: Long, apiKey: String? = null): Mono<String> = get("/v1/applications/$id", apiKey)

    private fun get(path: String, apiKey: String?): Mono<String> {
        val request = webClient.get().uri(path)
        
        if (!apiKey.isNullOrBlank()) {
            request.header(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
        }

        return request
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
                // Check if global uri is configured, though here we might be using per-guild context
                // Leaving this check but it might be redundant if we assume apiKey is sufficient or handled by caller validation
            }
    }
}
