package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken
import com.edgerush.lootman.domain.auth.repository.RefreshTokenRepository
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.shared.InvalidRefreshTokenException
import com.edgerush.lootman.domain.shared.UserNotFoundException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

/**
 * Service for handling user authentication and JWT token management.
 */
@Service
@Transactional
class AuthenticationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oauth2Service: OAuth2Service,
    private val properties: OAuth2Properties,
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)
    private val secureRandom = SecureRandom()

    private val jwtKey: SecretKey by lazy {
        val secret =
            properties.jwt.secret.ifBlank {
                // Generate a random key if not configured (for development)
                logger.warn("JWT secret not configured, using random key. Sessions will not persist across restarts.")
                Base64.getEncoder().encodeToString(ByteArray(64).also { secureRandom.nextBytes(it) })
            }
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    }

    // ============= Discord Authentication =============

    /**
     * Gets the Discord OAuth2 authorization URL.
     */
    fun getDiscordAuthUrl(state: String? = null): OAuth2UrlResponse {
        return OAuth2UrlResponse(
            url = oauth2Service.getDiscordAuthorizationUrl(state),
            provider = "discord",
        )
    }

    /**
     * Authenticates a user via Discord OAuth2 callback.
     */
    fun authenticateWithDiscord(code: String): TokenResponse {
        val discordUser = oauth2Service.exchangeDiscordCode(code)

        // Find or create user
        val user =
            userRepository.findByDiscordId(discordUser.id)
                ?.let { existingUser ->
                    // Update user profile from Discord
                    userRepository.save(
                        existingUser
                            .updateProfile(
                                username = discordUser.username,
                                email = discordUser.email,
                                avatarUrl = discordUser.avatarUrl,
                            )
                            .recordLogin(),
                    )
                }
                ?: userRepository.save(
                    User.fromDiscord(
                        discordId = discordUser.id,
                        username = discordUser.username,
                        email = discordUser.email,
                        avatarUrl = discordUser.avatarUrl,
                    ).recordLogin(),
                )

        return generateTokens(user)
    }

    // ============= Battle.net Authentication =============

    /**
     * Gets the Battle.net OAuth2 authorization URL.
     */
    fun getBattlenetAuthUrl(state: String? = null): OAuth2UrlResponse {
        return OAuth2UrlResponse(
            url = oauth2Service.getBattlenetAuthorizationUrl(state),
            provider = "battlenet",
        )
    }

    /**
     * Authenticates a user via Battle.net OAuth2 callback.
     */
    fun authenticateWithBattlenet(code: String): TokenResponse {
        val battlenetUser = oauth2Service.exchangeBattlenetCode(code)

        // Find or create user
        val user =
            userRepository.findByBattlenetId(battlenetUser.sub)
                ?.let { existingUser ->
                    // Update user profile from Battle.net
                    userRepository.save(
                        existingUser
                            .updateProfile(username = battlenetUser.battletag)
                            .recordLogin(),
                    )
                }
                ?: userRepository.save(
                    User.fromBattlenet(
                        battlenetId = battlenetUser.sub,
                        username = battlenetUser.battletag,
                    ).recordLogin(),
                )

        return generateTokens(user)
    }

    // ============= Token Management =============

    /**
     * Refreshes an access token using a refresh token.
     */
    fun refreshAccessToken(refreshToken: String): TokenResponse {
        val tokenHash = hashToken(refreshToken)
        val storedToken =
            refreshTokenRepository.findByTokenHash(tokenHash)
                ?: throw InvalidRefreshTokenException("Refresh token not found")

        if (!storedToken.isValid()) {
            throw InvalidRefreshTokenException("Refresh token is expired or revoked")
        }

        val user =
            userRepository.findById(storedToken.userId)
                ?: throw InvalidRefreshTokenException("User not found for refresh token")

        // Revoke the old refresh token and generate new tokens
        refreshTokenRepository.save(storedToken.revoke())

        return generateTokens(user)
    }

    /**
     * Logs out a user by revoking all their refresh tokens.
     */
    fun logout(userId: UserId): LogoutResponse {
        val revokedCount = refreshTokenRepository.revokeAllByUserId(userId)
        logger.info("Logged out user ${userId.value}, revoked $revokedCount refresh tokens")
        return LogoutResponse(success = true)
    }

    /**
     * Gets the current user profile from a JWT token.
     */
    @Transactional(readOnly = true)
    fun getCurrentUser(token: String): UserProfileResponse {
        val claims = parseToken(token)
        val userId = UserId((claims.subject).toLong())

        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException(userId.value)

        return UserProfileResponse.from(user)
    }

    /**
     * Validates a JWT token and returns the user ID if valid.
     */
    fun validateToken(token: String): UserId? {
        return try {
            val claims = parseToken(token)
            UserId(claims.subject.toLong())
        } catch (e: Exception) {
            logger.debug("Token validation failed: ${e.message}")
            null
        }
    }

    // ============= Private Methods =============

    private fun generateTokens(user: User): TokenResponse {
        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken(user)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = properties.jwt.accessTokenValidityMinutes * 60,
        )
    }

    private fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(properties.jwt.accessTokenValidityMinutes * 60)

        return Jwts.builder()
            .subject(user.id!!.value.toString())
            .claim("username", user.username)
            .claim("role", user.role.name)
            .claim("guildId", user.guildId?.value)
            .issuer(properties.jwt.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(jwtKey)
            .compact()
    }

    private fun generateRefreshToken(user: User): String {
        // Generate a cryptographically secure random token
        val rawToken = ByteArray(64).also { secureRandom.nextBytes(it) }
        val tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken)
        val tokenHash = hashToken(tokenString)

        // Store the hashed token
        val refreshToken =
            UserRefreshToken.create(
                userId = user.id!!,
                tokenHash = tokenHash,
                validityDays = properties.jwt.refreshTokenValidityDays,
            )
        refreshTokenRepository.save(refreshToken)

        return tokenString
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(jwtKey)
            .requireIssuer(properties.jwt.issuer)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }
}
