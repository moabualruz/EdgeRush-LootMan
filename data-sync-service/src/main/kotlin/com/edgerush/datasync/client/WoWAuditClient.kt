package com.edgerush.datasync.client

import com.edgerush.datasync.config.SyncProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory

sealed class WoWAuditClientException(message: String) : RuntimeException(message)
class WoWAuditRateLimitException(message: String) : WoWAuditClientException(message)
class WoWAuditServerException(message: String) : WoWAuditClientException(message)
class WoWAuditClientErrorException(message: String) : WoWAuditClientException(message)
class WoWAuditUnexpectedResponse(message: String) : WoWAuditClientException(message)

@Component
class WoWAuditClient(
    private val webClient: WebClient,
    private val properties: SyncProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchRoster(): Mono<String> = get("/v1/characters")

    fun fetchLootHistory(guildId: Long, teamId: Long, seasonId: Long): Mono<String> =
        get("/api/guilds/$guildId/teams/$teamId/loot_history.json?season_id=$seasonId")

    fun fetchWishlists(): Mono<String> = get("/v1/wishlists")

    fun fetchWishlistDetail(id: Long): Mono<String> = get("/v1/wishlists/$id")

    fun fetchTeam(): Mono<String> = get("/v1/team")

    fun fetchPeriod(): Mono<String> = get("/v1/period")

    fun fetchAttendance(): Mono<String> = get("/v1/attendance")

    fun fetchRaids(includePast: Boolean = true): Mono<String> =
        get(if (includePast) "/v1/raids?include_past=true" else "/v1/raids")

    fun fetchRaidDetail(id: Long): Mono<String> = get("/v1/raids/$id")

    fun fetchHistoricalData(periodId: Long): Mono<String> =
        get("/v1/historical_data?period=$periodId")

    fun fetchGuests(): Mono<String> = get("/v1/guests")

    fun fetchApplications(): Mono<String> = get("/v1/applications")

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
