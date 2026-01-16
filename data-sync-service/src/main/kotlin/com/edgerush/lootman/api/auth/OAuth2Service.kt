package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.shared.OAuth2AuthenticationException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Service for handling OAuth2 authentication flows with Discord and Battle.net.
 */
@Service
class OAuth2Service(
    private val properties: OAuth2Properties,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)

    // ============= Discord OAuth2 =============

    /**
     * Generates the Discord OAuth2 authorization URL.
     */
    fun getDiscordAuthorizationUrl(state: String? = null): String {
        if (!properties.discord.isConfigured()) {
            throw OAuth2AuthenticationException("Discord", "Discord OAuth2 is not configured")
        }

        return UriComponentsBuilder.fromHttpUrl(properties.discord.authorizationUrl)
            .queryParam("client_id", properties.discord.clientId)
            .queryParam("redirect_uri", URLEncoder.encode(properties.discord.redirectUri, StandardCharsets.UTF_8))
            .queryParam("response_type", "code")
            .queryParam("scope", URLEncoder.encode(properties.discord.scopes, StandardCharsets.UTF_8))
            .apply { state?.let { queryParam("state", it) } }
            .build(true)
            .toUriString()
    }

    /**
     * Exchanges a Discord authorization code for user info.
     */
    fun exchangeDiscordCode(code: String): DiscordUserInfo {
        if (!properties.discord.isConfigured()) {
            throw OAuth2AuthenticationException("Discord", "Discord OAuth2 is not configured")
        }

        // Exchange code for access token
        val tokenResponse = exchangeDiscordCodeForToken(code)
        val accessToken =
            tokenResponse["access_token"] as? String
                ?: throw OAuth2AuthenticationException("Discord", "No access token in response")

        // Get user info
        return getDiscordUserInfo(accessToken)
    }

    private fun exchangeDiscordCodeForToken(code: String): Map<*, *> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

        val body =
            LinkedMultiValueMap<String, String>().apply {
                add("client_id", properties.discord.clientId)
                add("client_secret", properties.discord.clientSecret)
                add("grant_type", "authorization_code")
                add("code", code)
                add("redirect_uri", properties.discord.redirectUri)
            }

        try {
            val response =
                restTemplate.postForEntity(
                    properties.discord.tokenUrl,
                    HttpEntity(body, headers),
                    Map::class.java,
                )

            return response.body ?: throw OAuth2AuthenticationException("Discord", "Empty token response")
        } catch (e: RestClientException) {
            logger.error("Discord token exchange failed", e)
            throw OAuth2AuthenticationException("Discord", "Token exchange failed: ${e.message}")
        }
    }

    private fun getDiscordUserInfo(accessToken: String): DiscordUserInfo {
        val headers =
            HttpHeaders().apply {
                setBearerAuth(accessToken)
            }

        try {
            val response =
                restTemplate.exchange(
                    properties.discord.userInfoUrl,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    Map::class.java,
                )

            val body = response.body ?: throw OAuth2AuthenticationException("Discord", "Empty user info response")

            return DiscordUserInfo(
                id = body["id"] as String,
                username = body["username"] as String,
                discriminator = body["discriminator"] as? String ?: "0",
                avatar = body["avatar"] as? String,
                email = body["email"] as? String,
                verified = body["verified"] as? Boolean,
            )
        } catch (e: RestClientException) {
            logger.error("Discord user info fetch failed", e)
            throw OAuth2AuthenticationException("Discord", "User info fetch failed: ${e.message}")
        }
    }

    // ============= Battle.net OAuth2 =============

    /**
     * Generates the Battle.net OAuth2 authorization URL.
     * Note: Battle.net requires the state parameter for CSRF protection.
     */
    fun getBattlenetAuthorizationUrl(state: String? = null): String {
        if (!properties.battlenet.isConfigured()) {
            throw OAuth2AuthenticationException("Battle.net", "Battle.net OAuth2 is not configured")
        }

        // Battle.net requires state parameter - generate one if not provided
        val stateValue = state ?: java.util.UUID.randomUUID().toString()

        return UriComponentsBuilder.fromHttpUrl(properties.battlenet.authorizationUrl)
            .queryParam("client_id", properties.battlenet.clientId)
            .queryParam("redirect_uri", URLEncoder.encode(properties.battlenet.redirectUri, StandardCharsets.UTF_8))
            .queryParam("response_type", "code")
            .queryParam("scope", URLEncoder.encode("openid wow.profile", StandardCharsets.UTF_8))
            .queryParam("state", stateValue)
            .build(true)
            .toUriString()
    }

    /**
     * Exchanges a Battle.net authorization code for user info and access token.
     */
    fun exchangeBattlenetCode(code: String): BattlenetAuthResult {
        if (!properties.battlenet.isConfigured()) {
            throw OAuth2AuthenticationException("Battle.net", "Battle.net OAuth2 is not configured")
        }

        // Exchange code for access token
        val tokenResponse = exchangeBattlenetCodeForToken(code)
        val accessToken =
            tokenResponse["access_token"] as? String
                ?: throw OAuth2AuthenticationException("Battle.net", "No access token in response")

        // Get user info
        val userInfo = getBattlenetUserInfo(accessToken)
        
        return BattlenetAuthResult(userInfo, accessToken)
    }

    private fun exchangeBattlenetCodeForToken(code: String): Map<*, *> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                setBasicAuth(properties.battlenet.clientId, properties.battlenet.clientSecret)
            }

        val body =
            LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("code", code)
                add("redirect_uri", properties.battlenet.redirectUri)
            }

        try {
            val response =
                restTemplate.postForEntity(
                    properties.battlenet.tokenUrl,
                    HttpEntity(body, headers),
                    Map::class.java,
                )

            return response.body ?: throw OAuth2AuthenticationException("Battle.net", "Empty token response")
        } catch (e: RestClientException) {
            logger.error("Battle.net token exchange failed", e)
            throw OAuth2AuthenticationException("Battle.net", "Token exchange failed: ${e.message}")
        }
    }

    private fun getBattlenetUserInfo(accessToken: String): BattlenetUserInfo {
        val headers =
            HttpHeaders().apply {
                setBearerAuth(accessToken)
            }

        try {
            val response =
                restTemplate.exchange(
                    properties.battlenet.userInfoUrl,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    Map::class.java,
                )

            val body = response.body ?: throw OAuth2AuthenticationException("Battle.net", "Empty user info response")

            return BattlenetUserInfo(
                sub = body["sub"] as String,
                id = (body["id"] as Number).toLong(),
                battletag = body["battletag"] as String,
            )
        } catch (e: RestClientException) {
            logger.error("Battle.net user info fetch failed", e)
            throw OAuth2AuthenticationException("Battle.net", "User info fetch failed: ${e.message}")
        }
    }
}

data class BattlenetAuthResult(
    val userInfo: BattlenetUserInfo,
    val accessToken: String
)
