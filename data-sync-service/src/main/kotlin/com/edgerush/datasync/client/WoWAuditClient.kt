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
    private val properties: SyncProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchRoster(): Mono<String> = get("/v1/characters")

    fun fetchLootHistory(seasonId: Long): Mono<String> =
        get("/v1/loot_history/$seasonId")

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

    private val baseUrlRequiresApiPrefix: Boolean = false

    private fun get(path: String): Mono<String> {
        val resolvedPath = resolvePath(path)
        return webClient
            .get()
            .uri(resolvedPath)
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
            .flatMap { body ->
                val snippet = body.trim()
                if (looksLikeHtml(snippet)) {
                    val redirectTarget = extractRedirectTarget(snippet)
                    val message = buildString {
                        append("WoWAudit request to '$resolvedPath' returned HTML instead of JSON.")
                        redirectTarget?.let { append(" Response redirected to '$it'.") }
                        append(" Check that sync.wowaudit.base-url points to the API (e.g. https://wowaudit.com)")
                        append(" and that a valid API key is configured if required. Sample response: ${snippet.take(200)}")
                    }
                    log.warn(message)
                    return@flatMap Mono.error(WoWAuditUnexpectedResponse(message))
                }
                Mono.just(body)
            }
            .doOnSubscribe {
                require(!properties.wowaudit.guildProfileUri.isNullOrBlank()) {
                    "sync.wowaudit.guild-profile-uri must be configured"
                }
            }
    }

    private fun resolvePath(path: String): String {
        if (baseUrlRequiresApiPrefix && path.startsWith("/v1/")) {
            return "$path"
        }
        return path
    }

    private fun looksLikeHtml(payload: String): Boolean =
        payload.startsWith("<!DOCTYPE", ignoreCase = true) ||
            payload.startsWith("<html", ignoreCase = true)

    private fun extractRedirectTarget(payload: String): String? =
        Regex("href=\"([^\"]+)\"").find(payload)?.groupValues?.getOrNull(1)
}
