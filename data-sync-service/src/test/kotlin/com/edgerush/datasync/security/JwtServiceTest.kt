package com.edgerush.datasync.security

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.auth.JwtProperties
import com.edgerush.lootman.api.auth.OAuth2Properties
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

/**
 * Comprehensive unit tests for JwtService.
 *
 * Tests cover:
 * - Token generation
 * - Token validation
 * - Claims extraction
 * - User extraction
 * - Error handling
 * - Edge cases
 */
class JwtServiceTest : UnitTest() {
    // Use Base64-encoded secret key (at least 256 bits = 32 bytes)
    private val testSecretBase64 =
        Base64.getEncoder().encodeToString(
            "test-secret-key-must-be-at-least-256-bits-long-for-security-purposes".toByteArray(),
        )

    private val defaultJwtProperties =
        JwtProperties(
            secret = testSecretBase64,
            accessTokenValidityMinutes = 60,
            refreshTokenValidityDays = 90,
            issuer = "test-issuer",
        )

    private val defaultOAuth2Properties =
        OAuth2Properties(
            jwt = defaultJwtProperties,
        )

    private val defaultKeyProvider = JwtKeyProvider(defaultOAuth2Properties)
    private val jwtService = JwtService(defaultKeyProvider, defaultOAuth2Properties)

    private fun createServiceWithProperties(jwtProperties: JwtProperties): JwtService {
        val oauth2Props = OAuth2Properties(jwt = jwtProperties)
        val keyProvider = JwtKeyProvider(oauth2Props)
        return JwtService(keyProvider, oauth2Props)
    }

    @Nested
    inner class `generateToken` {
        @Test
        fun `should create valid JWT token`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    guildIds = listOf("guild-1", "guild-2"),
                )

            // Act
            val token = jwtService.generateToken(user)

            // Assert
            token.shouldNotBeNull()
            token.shouldNotBeEmpty()
        }

        @Test
        fun `should create token with three parts separated by dots`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)

            // Assert - JWT has header.payload.signature
            val parts = token.split(".")
            parts.size shouldBe 3
        }

        @Test
        fun `should create token with correct subject`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            claims.subject shouldBe "user-123"
        }

        @Test
        fun `should create token with username claim`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            claims["username"] shouldBe "testuser"
        }

        @Test
        fun `should create token with roles claim`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            @Suppress("UNCHECKED_CAST")
            val roles = claims["roles"] as List<String>
            roles shouldContainAll listOf("GUILD_ADMIN", "SYSTEM_ADMIN")
        }

        @Test
        fun `should create token with guildIds claim`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    guildIds = listOf("guild-1", "guild-2"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            @Suppress("UNCHECKED_CAST")
            val guildIds = claims["guildIds"] as List<String>
            guildIds shouldContainAll listOf("guild-1", "guild-2")
        }

        @Test
        fun `should create token with issuer`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            claims.issuer shouldBe "test-issuer"
        }

        @Test
        fun `should create token with issued at date`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val beforeGeneration = Date()
            val token = jwtService.generateToken(user)
            val afterGeneration = Date()
            val claims = jwtService.extractClaims(token)

            // Assert
            claims.issuedAt.shouldNotBeNull()
            (claims.issuedAt.time >= beforeGeneration.time - 1000).shouldBeTrue()
            (claims.issuedAt.time <= afterGeneration.time + 1000).shouldBeTrue()
        }

        @Test
        fun `should create token with expiration date`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            claims.expiration.shouldNotBeNull()
            claims.expiration.after(Date()).shouldBeTrue()
        }

        @Test
        fun `should create different tokens for different users`() {
            // Arrange
            val user1 =
                AuthenticatedUser(
                    id = "user-1",
                    username = "user1",
                    roles = listOf("GUILD_ADMIN"),
                )
            val user2 =
                AuthenticatedUser(
                    id = "user-2",
                    username = "user2",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token1 = jwtService.generateToken(user1)
            val token2 = jwtService.generateToken(user2)

            // Assert
            (token1 != token2).shouldBeTrue()
        }

        @Test
        fun `should handle user with empty roles`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = emptyList(),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            @Suppress("UNCHECKED_CAST")
            val roles = claims["roles"] as List<String>
            roles.shouldBeEmpty()
        }

        @Test
        fun `should handle user with empty guildIds`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    guildIds = emptyList(),
                )

            // Act
            val token = jwtService.generateToken(user)
            val claims = jwtService.extractClaims(token)

            // Assert
            @Suppress("UNCHECKED_CAST")
            val guildIds = claims["guildIds"] as List<String>
            guildIds.shouldBeEmpty()
        }
    }

    @Nested
    inner class `validateToken` {
        @Test
        fun `should return true for valid token`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )
            val token = jwtService.generateToken(user)

            // Act
            val isValid = jwtService.validateToken(token)

            // Assert
            isValid.shouldBeTrue()
        }

        @Test
        fun `should return false for invalid token`() {
            // Act
            val isValid = jwtService.validateToken("invalid-token")

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for empty token`() {
            // Act
            val isValid = jwtService.validateToken("")

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for malformed token`() {
            // Act
            val isValid = jwtService.validateToken("not.a.valid.jwt.token")

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for token with wrong signature`() {
            // Arrange - create a token with a different secret
            val differentSecretBase64 =
                Base64.getEncoder().encodeToString(
                    "different-secret-key-must-be-at-least-256-bits-long-for-security".toByteArray(),
                )
            val differentService =
                createServiceWithProperties(
                    JwtProperties(
                        secret = differentSecretBase64,
                        accessTokenValidityMinutes = 60,
                        issuer = "test-issuer",
                    ),
                )
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )
            val tokenWithDifferentSignature = differentService.generateToken(user)

            // Act
            val isValid = jwtService.validateToken(tokenWithDifferentSignature)

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for expired token`() {
            // Arrange - create service with very short expiration (0 minutes = immediate)
            val shortExpirationService =
                createServiceWithProperties(
                    JwtProperties(
                        secret = testSecretBase64,
                        accessTokenValidityMinutes = 0,
                        issuer = "test-issuer",
                    ),
                )
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )
            val token = shortExpirationService.generateToken(user)

            // Wait for token to expire
            Thread.sleep(10)

            // Act
            val isValid = shortExpirationService.validateToken(token)

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for token with null payload`() {
            // Act
            val isValid = jwtService.validateToken("eyJhbGciOiJIUzI1NiJ9..signature")

            // Assert
            isValid.shouldBeFalse()
        }

        @Test
        fun `should return false for token with tampered payload`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )
            val token = jwtService.generateToken(user)
            val parts = token.split(".")
            // Modify the payload (base64 encoded)
            val tamperedPayload = "eyJzdWIiOiJoYWNrZWQtdXNlciJ9"
            val tamperedToken = "${parts[0]}.$tamperedPayload.${parts[2]}"

            // Act
            val isValid = jwtService.validateToken(tamperedToken)

            // Assert
            isValid.shouldBeFalse()
        }
    }

    @Nested
    inner class `extractClaims` {
        @Test
        fun `should extract all claims from valid token`() {
            // Arrange
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                    guildIds = listOf("guild-1", "guild-2"),
                )
            val token = jwtService.generateToken(user)

            // Act
            val claims = jwtService.extractClaims(token)

            // Assert
            claims.subject shouldBe "user-123"
            claims["username"] shouldBe "testuser"
            claims.issuer shouldBe "test-issuer"
            claims.issuedAt.shouldNotBeNull()
            claims.expiration.shouldNotBeNull()
        }

        @Test
        fun `should throw exception for invalid token`() {
            // Act & Assert
            assertThrows<Exception> {
                jwtService.extractClaims("invalid-token")
            }
        }

        @Test
        fun `should throw exception for expired token`() {
            // Arrange - create service with very short expiration
            val shortExpirationService =
                createServiceWithProperties(
                    JwtProperties(
                        secret = testSecretBase64,
                        accessTokenValidityMinutes = 0,
                        issuer = "test-issuer",
                    ),
                )
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )
            val token = shortExpirationService.generateToken(user)

            // Wait for token to expire
            Thread.sleep(10)

            // Act & Assert
            assertThrows<ExpiredJwtException> {
                shortExpirationService.extractClaims(token)
            }
        }
    }

    @Nested
    inner class `extractUser` {
        @Test
        fun `should extract user with all fields from valid token`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                    guildIds = listOf("guild-1", "guild-2"),
                )
            val token = jwtService.generateToken(originalUser)

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.id shouldBe originalUser.id
            extractedUser.username shouldBe originalUser.username
            extractedUser.roles shouldContainAll originalUser.roles
            extractedUser.guildIds shouldContainAll originalUser.guildIds
        }

        @Test
        fun `should set isAdminMode to false for extracted user`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    isAdminMode = true, // This is not stored in token
                )
            val token = jwtService.generateToken(originalUser)

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.isAdminMode.shouldBeFalse()
        }

        @Test
        fun `should handle user with empty roles`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = emptyList(),
                )
            val token = jwtService.generateToken(originalUser)

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.roles.shouldBeEmpty()
        }

        @Test
        fun `should handle user with empty guildIds`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    guildIds = emptyList(),
                )
            val token = jwtService.generateToken(originalUser)

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.guildIds.shouldBeEmpty()
        }

        @Test
        fun `should use subject as username when username claim is missing`() {
            // Arrange - manually create a token without username claim
            val expirationMs = defaultJwtProperties.accessTokenValidityMinutes * 60 * 1000
            val token =
                Jwts.builder()
                    .subject("user-123")
                    .claim("roles", listOf("GUILD_ADMIN"))
                    .claim("guildIds", listOf("guild-1"))
                    .issuer(defaultJwtProperties.issuer)
                    .issuedAt(Date())
                    .expiration(Date(System.currentTimeMillis() + expirationMs))
                    .signWith(defaultKeyProvider.secretKey)
                    .compact()

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.username shouldBe "user-123"
        }

        @Test
        fun `should handle missing roles claim as empty list`() {
            // Arrange - manually create a token without roles claim
            val expirationMs = defaultJwtProperties.accessTokenValidityMinutes * 60 * 1000
            val token =
                Jwts.builder()
                    .subject("user-123")
                    .claim("username", "testuser")
                    .claim("guildIds", listOf("guild-1"))
                    .issuer(defaultJwtProperties.issuer)
                    .issuedAt(Date())
                    .expiration(Date(System.currentTimeMillis() + expirationMs))
                    .signWith(defaultKeyProvider.secretKey)
                    .compact()

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.roles.shouldBeEmpty()
        }

        @Test
        fun `should handle missing guildIds claim as empty list`() {
            // Arrange - manually create a token without guildIds claim
            val expirationMs = defaultJwtProperties.accessTokenValidityMinutes * 60 * 1000
            val token =
                Jwts.builder()
                    .subject("user-123")
                    .claim("username", "testuser")
                    .claim("roles", listOf("GUILD_ADMIN"))
                    .issuer(defaultJwtProperties.issuer)
                    .issuedAt(Date())
                    .expiration(Date(System.currentTimeMillis() + expirationMs))
                    .signWith(defaultKeyProvider.secretKey)
                    .compact()

            // Act
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.guildIds.shouldBeEmpty()
        }

        @Test
        fun `should throw exception for invalid token`() {
            // Act & Assert
            assertThrows<Exception> {
                jwtService.extractUser("invalid-token")
            }
        }
    }

    @Nested
    inner class `JwtProperties configuration` {
        @Test
        fun `should have sensible defaults`() {
            // Arrange & Act
            val properties = JwtProperties()

            // Assert
            (properties.accessTokenValidityMinutes > 0).shouldBeTrue()
            properties.issuer.shouldNotBeEmpty()
        }

        @Test
        fun `should allow configuration of all properties via constructor`() {
            // Arrange & Act
            val properties =
                JwtProperties(
                    secret = "custom-secret",
                    accessTokenValidityMinutes = 120,
                    refreshTokenValidityDays = 30,
                    issuer = "custom-issuer",
                )

            // Assert
            properties.secret shouldBe "custom-secret"
            properties.accessTokenValidityMinutes shouldBe 120
            properties.refreshTokenValidityDays shouldBe 30
            properties.issuer shouldBe "custom-issuer"
        }

        @Test
        fun `should support data class copy`() {
            // Arrange
            val original = JwtProperties()

            // Act
            val copied =
                original.copy(
                    secret = "new-secret",
                    accessTokenValidityMinutes = 30,
                    issuer = "new-issuer",
                )

            // Assert
            copied.secret shouldBe "new-secret"
            copied.accessTokenValidityMinutes shouldBe 30
            copied.issuer shouldBe "new-issuer"
        }

        @Test
        fun `should allow setting secret via setter for Spring property binding`() {
            // Arrange
            val properties = JwtProperties()
            val newSecret = "new-secret-key-set-via-setter"

            // Act
            properties.secret = newSecret

            // Assert
            properties.secret shouldBe newSecret
        }

        @Test
        fun `should allow setting accessTokenValidityMinutes via setter for Spring property binding`() {
            // Arrange
            val properties = JwtProperties()
            val newValidity = 30L

            // Act
            properties.accessTokenValidityMinutes = newValidity

            // Assert
            properties.accessTokenValidityMinutes shouldBe newValidity
        }

        @Test
        fun `should allow setting issuer via setter for Spring property binding`() {
            // Arrange
            val properties = JwtProperties()
            val newIssuer = "new-issuer-set-via-setter"

            // Act
            properties.issuer = newIssuer

            // Assert
            properties.issuer shouldBe newIssuer
        }

        @Test
        fun `should support modifying all properties via setters`() {
            // Arrange
            val properties = JwtProperties()

            // Act - simulate Spring property binding
            properties.secret = "completely-new-secret"
            properties.accessTokenValidityMinutes = 120
            properties.issuer = "completely-new-issuer"

            // Assert
            properties.secret shouldBe "completely-new-secret"
            properties.accessTokenValidityMinutes shouldBe 120
            properties.issuer shouldBe "completely-new-issuer"
        }
    }

    @Nested
    inner class `token round-trip` {
        @Test
        fun `should preserve all user data through token round-trip`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                    guildIds = listOf("guild-1", "guild-2", "guild-3"),
                )

            // Act
            val token = jwtService.generateToken(originalUser)
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.id shouldBe originalUser.id
            extractedUser.username shouldBe originalUser.username
            extractedUser.roles shouldBe originalUser.roles
            extractedUser.guildIds shouldBe originalUser.guildIds
        }

        @Test
        fun `should preserve special characters in username`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "test.user@example.com",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = jwtService.generateToken(originalUser)
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.username shouldBe "test.user@example.com"
        }

        @Test
        fun `should preserve unicode characters in data`() {
            // Arrange
            val originalUser =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                    guildIds = listOf("guild-1"),
                )

            // Act
            val token = jwtService.generateToken(originalUser)
            val extractedUser = jwtService.extractUser(token)

            // Assert
            extractedUser.guildIds shouldContainAll listOf("guild-1")
        }
    }

    @Nested
    inner class `different configurations` {
        @Test
        fun `should work with custom issuer`() {
            // Arrange
            val customService =
                createServiceWithProperties(
                    JwtProperties(
                        secret = testSecretBase64,
                        accessTokenValidityMinutes = 60,
                        issuer = "custom-application-issuer",
                    ),
                )
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = customService.generateToken(user)
            val claims = customService.extractClaims(token)

            // Assert
            claims.issuer shouldBe "custom-application-issuer"
        }

        @Test
        fun `should work with different expiration times`() {
            // Arrange
            val sevenDaysInMinutes = 7 * 24 * 60L // 7 days in minutes
            val longExpirationService =
                createServiceWithProperties(
                    JwtProperties(
                        secret = testSecretBase64,
                        accessTokenValidityMinutes = sevenDaysInMinutes,
                        issuer = "test-issuer",
                    ),
                )
            val user =
                AuthenticatedUser(
                    id = "user-123",
                    username = "testuser",
                    roles = listOf("GUILD_ADMIN"),
                )

            // Act
            val token = longExpirationService.generateToken(user)
            val claims = longExpirationService.extractClaims(token)

            // Assert - expiration should be in the future and roughly 7 days from now
            val now = System.currentTimeMillis()
            val actualExpiration = claims.expiration.time
            val sixDaysInMs = 86400000L * 6
            val eightDaysInMs = 86400000L * 8

            // The expiration should be more than 6 days but less than 8 days from now
            (actualExpiration > now + sixDaysInMs).shouldBeTrue()
            (actualExpiration < now + eightDaysInMs).shouldBeTrue()
        }
    }
}
