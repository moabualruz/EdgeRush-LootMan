package com.edgerush.lootman.domain.application.client

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.scheduler.Schedulers
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Provider for Warcraft Logs OAuth2 access tokens.
 *
 * Uses client credentials flow to obtain and cache access tokens.
 */
interface WarcraftLogsTokenProvider {
    /**
     * Gets a valid access token, refreshing if necessary.
     */
    fun getAccessToken(): String
}

/**
 * Implementation of WarcraftLogsTokenProvider using OAuth2 client credentials.
 */
@Component
class OAuth2WarcraftLogsTokenProvider(
    webClientBuilder: WebClient.Builder,
    private val properties: WarcraftLogsProperties,
) : WarcraftLogsTokenProvider {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient =
        webClientBuilder
            .baseUrl(properties.tokenUrl)
            .build()

    private var cachedToken: String? = null
    private var tokenExpiresAt: Instant = Instant.MIN
    private val lock = ReentrantLock()

    override fun getAccessToken(): String {
        lock.withLock {
            // Check if token is still valid (with 60 second buffer)
            if (cachedToken != null && Instant.now().plusSeconds(60).isBefore(tokenExpiresAt)) {
                return cachedToken!!
            }

            // Fetch new token
            return refreshToken()
        }
    }

    private fun refreshToken(): String {
        if (!properties.enabled) {
            throw IllegalStateException("Warcraft Logs integration is not enabled")
        }

        log.debug("Refreshing Warcraft Logs access token")

        val response =
            webClient
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", properties.clientId)
                        .with("client_secret", properties.clientSecret),
                )
                .retrieve()
                .bodyToMono(TokenResponse::class.java)
                .subscribeOn(Schedulers.boundedElastic())
                .block()
                ?: throw WarcraftLogsAuthException("Failed to obtain access token")

        cachedToken = response.accessToken
        tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn.toLong())

        log.debug("Successfully obtained Warcraft Logs access token, expires in {} seconds", response.expiresIn)

        return response.accessToken
    }

    private data class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("token_type")
        val tokenType: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
    )
}

class WarcraftLogsAuthException(message: String) : RuntimeException(message)
