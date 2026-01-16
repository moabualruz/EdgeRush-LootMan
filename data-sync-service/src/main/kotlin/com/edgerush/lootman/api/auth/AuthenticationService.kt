package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken
import com.edgerush.lootman.domain.auth.model.UserRole
import com.edgerush.lootman.domain.auth.repository.RefreshTokenRepository
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.shared.InvalidCredentialsException
import com.edgerush.lootman.domain.shared.InvalidRefreshTokenException
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.UserAlreadyExistsException
import com.edgerush.lootman.domain.shared.UserNotFoundException
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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
    private val blizzardDataService: com.edgerush.lootman.infrastructure.external.blizzard.BlizzardDataService,
    private val userCharacterRepository: com.edgerush.lootman.domain.auth.repository.UserCharacterRepository,
    private val raiderRepository: RaiderRepository,
    private val userCharacterMappingRepository: UserCharacterMappingRepository,
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)
    private val secureRandom = SecureRandom()
    private val passwordEncoder = BCryptPasswordEncoder()

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

    // ============= Account Linking =============

    /**
     * Links a Discord account to an existing user.
     *
     * @param userId The ID of the user to link
     * @param code The OAuth2 authorization code from Discord
     * @return Updated user profile
     * @throws UserNotFoundException if user doesn't exist
     * @throws UserAlreadyExistsException if Discord account is already linked to another user
     */
    fun linkDiscordAccount(
        userId: UserId,
        code: String,
    ): UserProfileResponse {
        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException(userId.value)

        val discordUser = oauth2Service.exchangeDiscordCode(code)

        // Check if this Discord ID is already linked to another user
        val existingUser = userRepository.findByDiscordId(discordUser.id)
        if (existingUser != null && existingUser.id != userId) {
            throw UserAlreadyExistsException("discordId", discordUser.id)
        }

        // Link Discord and update profile
        val updatedUser =
            userRepository.save(
                user
                    .linkDiscord(discordUser.id)
                    .updateProfile(
                        avatarUrl = discordUser.avatarUrl ?: user.avatarUrl,
                    ),
            )

        logger.info("Discord account linked for user ${userId.value}: ${discordUser.id}")
        return UserProfileResponse.from(updatedUser)
    }

    /**
     * Links a Battle.net account to an existing user and syncs characters.
     *
     * @param userId The ID of the user to link
     * @param code The OAuth2 authorization code from Battle.net
     * @return Updated user profile
     * @throws UserNotFoundException if user doesn't exist
     * @throws UserAlreadyExistsException if Battle.net account is already linked to another user
     */
    fun linkBattlenetAccount(
        userId: UserId,
        code: String,
    ): UserProfileResponse {
        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException(userId.value)

        val authResult = oauth2Service.exchangeBattlenetCode(code)
        val battlenetUser = authResult.userInfo

        // Check if this Battle.net ID is already linked to another user
        val existingUser = userRepository.findByBattlenetId(battlenetUser.sub)
        if (existingUser != null && existingUser.id != userId) {
            throw UserAlreadyExistsException("battlenetId", battlenetUser.sub)
        }

        // Link Battle.net account
        val updatedUser = userRepository.save(user.linkBattlenet(battlenetUser.sub))
        
        // Sync characters
        syncBattlenetCharacters(updatedUser, authResult.accessToken)

        logger.info("Battle.net account linked for user ${userId.value}: ${battlenetUser.sub}")
        return UserProfileResponse.from(updatedUser)
    }
    
    /**
     * Authenticates a user via Battle.net OAuth2 callback and syncs characters.
     */
    fun authenticateWithBattlenet(code: String): TokenResponse {
        val authResult = oauth2Service.exchangeBattlenetCode(code)
        val battlenetUser = authResult.userInfo

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

        // Sync characters
        syncBattlenetCharacters(user, authResult.accessToken)

        return generateTokens(user)
    }

    // ============= Local Authentication =============

    /**
     * Registers a new user with local credentials (username/password).
     *
     * @param username The desired username
     * @param email The user's email address
     * @param password The plain text password (will be hashed)
     * @return TokenResponse with access and refresh tokens
     * @throws UserAlreadyExistsException if username or email is already taken
     */
    fun registerLocal(
        username: String,
        email: String,
        password: String,
        role: String? = null,
    ): TokenResponse {
        // Validate username uniqueness
        if (userRepository.existsByUsername(username)) {
            throw UserAlreadyExistsException("username", username)
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException("email", email)
        }

        // Hash the password
        val passwordHash = passwordEncoder.encode(password)

        // Create and save the user
        val user =
            userRepository.save(
                User.fromLocal(
                    username = username,
                    email = email,
                    passwordHash = passwordHash,
                    role = role?.let { UserRole.fromString(it) } ?: UserRole.RAIDER,
                ).recordLogin(),
            )

        logger.info("New local user registered: ${user.username} (id=${user.id?.value})")
        return generateTokens(user)
    }

    /**
     * Authenticates a user with local credentials (username or email + password).
     *
     * @param usernameOrEmail The username or email address
     * @param password The plain text password
     * @return TokenResponse with access and refresh tokens
     * @throws InvalidCredentialsException if credentials are invalid
     */
    fun loginLocal(
        usernameOrEmail: String,
        password: String,
    ): TokenResponse {
        // Find user by username or email
        val user =
            userRepository.findByUsername(usernameOrEmail)
                ?: userRepository.findByEmail(usernameOrEmail)
                ?: throw InvalidCredentialsException()

        // Verify password
        if (!user.hasPassword() || !passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        // Update last login and return tokens
        val updatedUser = userRepository.save(user.recordLogin())
        logger.info("Local user logged in: ${updatedUser.username} (id=${updatedUser.id?.value})")
        return generateTokens(updatedUser)
    }
    
    private fun syncBattlenetCharacters(user: User, accessToken: String) {
        try {
            val characters = blizzardDataService.getAccountCharacters(accessToken)
            val userCharacters = characters.map { char ->
                com.edgerush.lootman.domain.auth.model.UserCharacter(
                    userId = user.id!!,
                    name = char.name,
                    realm = char.realm.name,
                    className = char.playable_class.name,  // Store raw class name from Blizzard
                    level = char.level,
                    race = char.playable_race.name,
                    faction = char.faction.name,
                    blizzardId = char.id
                )
            }.filter { it.level >= 70 } // Only sync max/near-max level chars to reduce noise, assuming TWW level cap is 80, 70 is decent start

            userCharacterRepository.saveAll(userCharacters)
            logger.info("Synced ${userCharacters.size} characters for user ${user.id!!.value}")

            // Auto-link characters to raiders in guild roster
            autoLinkCharactersToRaiders(user.id!!, userCharacters)
        } catch (e: Exception) {
            logger.error("Failed to sync Battle.net characters for user ${user.id?.value}", e)
            // Swallow exception to not block login/linking
        }
    }

    /**
     * Automatically links user's Battle.net characters to matching raiders in guild rosters.
     * This allows the character selector dropdown to show all characters with their guild roles.
     */
    private fun autoLinkCharactersToRaiders(userId: UserId, characters: List<com.edgerush.lootman.domain.auth.model.UserCharacter>) {
        var linkedCount = 0
        var skippedCount = 0

        for (character in characters) {
            try {
                // Find matching raider by character name and realm
                val raider = raiderRepository.findByCharacterNameAndRealm(character.name, character.realm)

                if (raider != null) {
                    val raiderId = raider.id  // Already a RaiderId

                    // Check if already linked
                    if (!userCharacterMappingRepository.existsByUserIdAndRaiderId(userId, raiderId)) {
                        // Check if this is the first character for the user
                        val isPrimary = userCharacterMappingRepository.countByUserId(userId) == 0L

                        val mapping = UserCharacterMapping.create(
                            userId = userId,
                            raiderId = raiderId,
                            isPrimary = isPrimary
                        )

                        userCharacterMappingRepository.save(mapping)
                        linkedCount++
                        logger.info("Auto-linked character ${character.name}-${character.realm} to raider ${raider.id.value} (guild: ${raider.guildId.value}, rank: ${raider.rank})")
                    } else {
                        skippedCount++
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to auto-link character ${character.name}-${character.realm}: ${e.message}")
            }
        }

        if (linkedCount > 0 || skippedCount > 0) {
            logger.info("Auto-link complete for user ${userId.value}: $linkedCount new links, $skippedCount already linked")
        }
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
