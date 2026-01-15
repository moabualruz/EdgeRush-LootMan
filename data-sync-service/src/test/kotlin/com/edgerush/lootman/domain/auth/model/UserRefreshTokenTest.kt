package com.edgerush.lootman.domain.auth.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for UserRefreshToken entity.
 */
class UserRefreshTokenTest : UnitTest() {
    private val userId = UserId(1L)
    private val tokenHash = "hashed-token-value"

    @Nested
    inner class Creation {
        @Test
        fun `should create token with default validity`() {
            // Act
            val token =
                UserRefreshToken.create(
                    userId = userId,
                    tokenHash = tokenHash,
                )

            // Assert
            token.userId shouldBe userId
            token.tokenHash shouldBe tokenHash
            token.expiresAt.isAfter(Instant.now()) shouldBe true
            token.revokedAt shouldBe null
        }

        @Test
        fun `should create token with custom validity`() {
            // Act
            val token =
                UserRefreshToken.create(
                    userId = userId,
                    tokenHash = tokenHash,
                    validityDays = 7,
                )

            // Assert
            val expectedExpiry = Instant.now().plusSeconds(7 * 24 * 60 * 60)
            val diff = kotlin.math.abs(token.expiresAt.epochSecond - expectedExpiry.epochSecond)
            (diff < 5) shouldBe true // Within 5 seconds
        }

        @Test
        fun `should require non-blank token hash`() {
            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                UserRefreshToken(
                    userId = userId,
                    tokenHash = "  ",
                    expiresAt = Instant.now().plusSeconds(3600),
                )
            }
        }
    }

    @Nested
    inner class Validation {
        @Test
        fun `should be valid when not expired and not revoked`() {
            // Arrange
            val token = UserRefreshToken.create(userId, tokenHash)

            // Assert
            token.isValid() shouldBe true
            token.isExpired() shouldBe false
            token.isRevoked() shouldBe false
        }

        @Test
        fun `should be invalid when expired`() {
            // Arrange
            val token =
                UserRefreshToken(
                    userId = userId,
                    tokenHash = tokenHash,
                    expiresAt = Instant.now().minusSeconds(3600),
                )

            // Assert
            token.isValid() shouldBe false
            token.isExpired() shouldBe true
        }

        @Test
        fun `should be invalid when revoked`() {
            // Arrange
            val token = UserRefreshToken.create(userId, tokenHash).revoke()

            // Assert
            token.isValid() shouldBe false
            token.isRevoked() shouldBe true
        }

        @Test
        fun `should use provided timestamp for validation`() {
            // Arrange
            val future = Instant.now().plusSeconds(3600)
            val token =
                UserRefreshToken(
                    userId = userId,
                    tokenHash = tokenHash,
                    expiresAt = Instant.now().plusSeconds(1800),
                )

            // Act & Assert
            token.isValid(Instant.now()) shouldBe true
            token.isValid(future) shouldBe false
        }
    }

    @Nested
    inner class Revocation {
        @Test
        fun `should set revokedAt when revoked`() {
            // Arrange
            val token = UserRefreshToken.create(userId, tokenHash)

            // Act
            val revoked = token.revoke()

            // Assert
            revoked.isRevoked() shouldBe true
            revoked.revokedAt shouldBe Instant.now()
        }
    }
}
