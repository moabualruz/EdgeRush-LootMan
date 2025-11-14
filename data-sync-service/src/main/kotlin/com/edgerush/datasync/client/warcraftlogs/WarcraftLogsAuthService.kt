package com.edgerush.datasync.client.warcraftlogs

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsProperties
import com.edgerush.datasync.service.warcraftlogs.ClientCredentials
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Service
class WarcraftLogsAuthService(
    private val properties: WarcraftLogsProperties,
    private val webClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tokenCache = ConcurrentHashMap<String, CachedToken>()

    suspend fun getAccessToken(credentials: ClientCredentials): String {
        val cacheKey = "${credentials.clientId}:${credentials.clientSecret}"
        val cached = tokenCache[cacheKey]

        // Return cached token if valid
        if (cached != null && cached.isValid()) {
            logger.debug("Using cached access token")
            return cached.accessToken
        }

        // Request new token
        logger.info("Requesting new Warcraft Logs access token")
        val token = requestAccessToken(credentials)

        // Cache the token
        tokenCache[cacheKey] =
            CachedToken(
                accessToken = token.accessToken,
                expiresAt = Instant.now().plusSeconds(token.expiresIn - 60), // 60s buffer
            )

        return token.accessToken
    }

    private suspend fun requestAccessToken(credentials: ClientCredentials): TokenResponse {
        val authHeader =
            "Basic " +
                Base64.getEncoder().encodeToString(
                    "${credentials.clientId}:${credentials.clientSecret}".toByteArray(),
                )

        return webClient.post()
            .uri(properties.tokenUrl)
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .awaitBody<TokenResponse>()
    }

    fun clearCache() {
        tokenCache.clear()
        logger.info("Cleared Warcraft Logs token cache")
    }

    private data class CachedToken(
        val accessToken: String,
        val expiresAt: Instant,
    ) {
        fun isValid(): Boolean = Instant.now().isBefore(expiresAt)
    }

    private data class TokenResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Long,
    ) {
        val accessToken: String get() = access_token
        val expiresIn: Long get() = expires_in
    }
}
