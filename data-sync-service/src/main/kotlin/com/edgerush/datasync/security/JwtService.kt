package com.edgerush.datasync.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "default-secret-key-change-in-production-must-be-at-least-256-bits",
    var expirationMs: Long = 86400000, // 24 hours
    var issuer: String = "edgerush-lootman",
)

@Service
class JwtService(
    private val properties: JwtProperties,
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(properties.secret.toByteArray())
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractUser(token: String): AuthenticatedUser {
        val claims = extractClaims(token)
        return AuthenticatedUser(
            id = claims.subject,
            username = claims["username"] as? String ?: claims.subject,
            roles = (claims["roles"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            guildIds = (claims["guildIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            isAdminMode = false,
        )
    }

    fun generateToken(user: AuthenticatedUser): String {
        val now = Date()
        val expiration = Date(now.time + properties.expirationMs)

        return Jwts.builder()
            .subject(user.id)
            .claim("username", user.username)
            .claim("roles", user.roles)
            .claim("guildIds", user.guildIds)
            .issuer(properties.issuer)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }
}
