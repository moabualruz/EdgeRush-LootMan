package com.edgerush.lootman.api.auth

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for OAuth2 authentication.
 */
@ConfigurationProperties(prefix = "oauth2")
data class OAuth2Properties(
    val discord: DiscordOAuth2Properties = DiscordOAuth2Properties(),
    val battlenet: BattlenetOAuth2Properties = BattlenetOAuth2Properties(),
    val jwt: JwtProperties = JwtProperties(),
)

/**
 * Discord OAuth2 configuration.
 */
data class DiscordOAuth2Properties(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
    val scopes: String = "identify email",
) {
    val authorizationUrl: String
        get() = "https://discord.com/api/oauth2/authorize"
    val tokenUrl: String
        get() = "https://discord.com/api/oauth2/token"
    val userInfoUrl: String
        get() = "https://discord.com/api/users/@me"

    fun isConfigured(): Boolean = clientId.isNotBlank() && clientSecret.isNotBlank()
}

/**
 * Battle.net OAuth2 configuration.
 */
data class BattlenetOAuth2Properties(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
    val region: String = "us",
) {
    val authorizationUrl: String
        get() = "https://$region.battle.net/oauth/authorize"
    val tokenUrl: String
        get() = "https://$region.battle.net/oauth/token"
    val userInfoUrl: String
        get() = "https://$region.battle.net/oauth/userinfo"

    fun isConfigured(): Boolean = clientId.isNotBlank() && clientSecret.isNotBlank()
}

/**
 * JWT token configuration.
 */
data class JwtProperties(
    val secret: String = "",
    val accessTokenValidityMinutes: Long = 15,
    val refreshTokenValidityDays: Long = 30,
    val issuer: String = "edgerush-lootman",
)
