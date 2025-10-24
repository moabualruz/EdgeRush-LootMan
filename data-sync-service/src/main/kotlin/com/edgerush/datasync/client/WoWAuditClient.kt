package com.edgerush.datasync.client

import com.edgerush.datasync.config.SyncProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

sealed class WoWAuditClientException(message: String) : RuntimeException(message)
class WoWAuditRateLimitException(message: String) : WoWAuditClientException(message)
class WoWAuditServerException(message: String) : WoWAuditClientException(message)

@Component
class WoWAuditClient(
    private val webClient: WebClient,
    private val properties: SyncProperties
) {

    fun fetchRoster(): Mono<String> = get("/guild/characters")

    fun fetchLootHistory(): Mono<String> = get("/guild/loot")

    fun fetchSimulations(): Mono<String> = get("/guild/sims")

    private fun get(path: String): Mono<String> = webClient
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
                .defaultIfEmpty("WoWAudit server error")
                .flatMap { Mono.error(WoWAuditServerException(it)) }
        }
        .bodyToMono(String::class.java)
        .doOnSubscribe {
            require(!properties.wowaudit.guildProfileUri.isNullOrBlank()) {
                "sync.wowaudit.guild-profile-uri must be configured"
            }
        }
}
