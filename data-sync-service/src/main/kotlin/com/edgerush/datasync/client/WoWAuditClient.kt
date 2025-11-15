package com.edgerush.datasync.client

import com.edgerush.datasync.config.SyncProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class WoWAuditClient(
    private val webClient: WebClient,
    private val properties: SyncProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val LOG_SNIPPET_LENGTH = 120
        private const val ERROR_SNIPPET_LENGTH = 200
    }

    fun fetchRoster(): Mono<String> = get("/v1/characters")

    fun fetchLootHistory(seasonId: Long): Mono<String> = get("/v1/loot_history/$seasonId")

    fun fetchWishlists(): Mono<String> = get("/v1/wishlists")

    fun fetchWishlistDetail(id: Long): Mono<String> = get("/v1/wishlists/$id")

    fun fetchTeam(): Mono<String> = get("/v1/team")

    fun fetchPeriod(): Mono<String> = get("/v1/period")

    fun fetchAttendance(): Mono<String> = get("/v1/attendance")

    fun fetchRaids(includePast: Boolean = true): Mono<String> = get(if (includePast) "/v1/raids?include_past=true" else "/v1/raids")

    fun fetchRaidDetail(id: Long): Mono<String> = get("/v1/raids/$id")

    fun fetchHistoricalData(periodId: Long): Mono<String> = get("/v1/historical_data?period=$periodId")

    fun fetchCharacterHistory(characterId: Long): Mono<String> = get("/v1/historical_data/$characterId")

    fun fetchGuests(): Mono<String> = get("/v1/guests")

    fun fetchApplications(): Mono<String> = get("/v1/applications")

    fun fetchApplicationDetail(id: Long): Mono<String> = get("/v1/applications/$id")

    private fun get(path: String): Mono<String> =
        webClient
            .get()
            .uri(path)
            .retrieve()
            .onStatus({ it == HttpStatus.TOO_MANY_REQUESTS }, ::handleRateLimitError)
            .onStatus({ it.is5xxServerError }, ::handleServerError)
            .onStatus({ it.is4xxClientError }, ::handleClientError)
            .bodyToMono(String::class.java)
            .map { body -> validateJsonResponse(body, path) }
            .doOnSubscribe { validateConfiguration() }

    private fun handleRateLimitError(response: org.springframework.web.reactive.function.client.ClientResponse): Mono<Throwable> {
        return response.bodyToMono(String::class.java)
            .defaultIfEmpty("WoWAudit rate limit hit")
            .flatMap { Mono.error(WoWAuditRateLimitException(it)) }
    }

    private fun handleServerError(response: org.springframework.web.reactive.function.client.ClientResponse): Mono<Throwable> {
        return response.bodyToMono(String::class.java)
            .defaultIfEmpty("WoWAudit server error (${response.statusCode()})")
            .flatMap { Mono.error(WoWAuditServerException(it)) }
    }

    private fun handleClientError(response: org.springframework.web.reactive.function.client.ClientResponse): Mono<Throwable> {
        return response.bodyToMono(String::class.java)
            .defaultIfEmpty("WoWAudit client error (${response.statusCode()})")
            .flatMap { body -> Mono.error(WoWAuditClientErrorException(body)) }
    }

    private fun validateJsonResponse(
        body: String,
        path: String,
    ): String {
        val snippet = body.trim()
        if (snippet.startsWith("<")) {
            log.warn(
                "WoWAudit response for '{}' was not JSON. First bytes: {}",
                path,
                snippet.take(LOG_SNIPPET_LENGTH),
            )
            throw WoWAuditUnexpectedResponse(
                "Expected JSON but received HTML. Snippet: ${snippet.take(ERROR_SNIPPET_LENGTH)}",
            )
        }
        return body
    }

    private fun validateConfiguration() {
        require(!properties.wowaudit.guildProfileUri.isNullOrBlank()) {
            "sync.wowaudit.guild-profile-uri must be configured"
        }
    }
}
