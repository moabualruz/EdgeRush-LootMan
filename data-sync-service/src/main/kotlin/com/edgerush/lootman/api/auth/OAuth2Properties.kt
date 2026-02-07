package com.edgerush.lootman.api.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for OAuth2 authentication.
 */
@Component
@ConfigurationProperties(prefix = "oauth2")
data class OAuth2Properties(
    var discord: DiscordOAuth2Properties = DiscordOAuth2Properties(),
    var battlenet: BattlenetOAuth2Properties = BattlenetOAuth2Properties(),
    var jwt: JwtProperties = JwtProperties(),
)

/**
 * Discord OAuth2 configuration.
 */
data class DiscordOAuth2Properties(
    var clientId: String = "",
    var clientSecret: String = "",
    var redirectUri: String = "",
    var scopes: String = "identify email",
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
    var clientId: String = "",
    var clientSecret: String = "",
    var redirectUri: String = "",
    var region: String = "us",
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
    var secret: String = "",
    var accessTokenValidityMinutes: Long = 15,
    var refreshTokenValidityDays: Long = 90,
    var issuer: String = "edgerush-lootman",
)
