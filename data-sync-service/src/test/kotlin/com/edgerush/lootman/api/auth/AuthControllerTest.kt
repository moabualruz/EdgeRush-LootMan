package com.edgerush.lootman.api.auth

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.InvalidRefreshTokenException
import com.edgerush.lootman.domain.shared.OAuth2AuthenticationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Unit tests for AuthController.
 */
class AuthControllerTest : UnitTest() {

    private lateinit var authenticationService: AuthenticationService
    private lateinit var controller: AuthController

    @BeforeEach
    fun setup() {
        authenticationService = mockk()
        controller = AuthController(authenticationService)
    }

    @Nested
    inner class DiscordAuth {

        @Test
        fun `should return Discord authorization URL`() {
            // Given
            val expected = OAuth2UrlResponse(
                url = "https://discord.com/api/oauth2/authorize?client_id=123",
                provider = "discord"
            )
            every { authenticationService.getDiscordAuthUrl(any()) } returns expected

            // When
            val result = controller.getDiscordAuthUrl(state = "test-state")

            // Then
            result.url shouldBe expected.url
            result.provider shouldBe "discord"
            verify(exactly = 1) { authenticationService.getDiscordAuthUrl("test-state") }
        }

        @Test
        fun `should exchange Discord code for tokens`() {
            // Given
            val request = OAuth2CallbackRequest(code = "auth-code-123")
            val expected = TokenResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 900
            )
            every { authenticationService.authenticateWithDiscord(request.code) } returns expected

            // When
            val result = controller.discordCallback(request)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.accessToken shouldBe "access-token"
            verify(exactly = 1) { authenticationService.authenticateWithDiscord("auth-code-123") }
        }

        @Test
        fun `should propagate OAuth2 exception`() {
            // Given
            val request = OAuth2CallbackRequest(code = "invalid-code")
            every { authenticationService.authenticateWithDiscord(any()) } throws
                OAuth2AuthenticationException("Discord", "Invalid code")

            // When & Then
            shouldThrow<OAuth2AuthenticationException> {
                controller.discordCallback(request)
            }
        }
    }

    @Nested
    inner class BattlenetAuth {

        @Test
        fun `should return Battlenet authorization URL`() {
            // Given
            val expected = OAuth2UrlResponse(
                url = "https://us.battle.net/oauth/authorize?client_id=123",
                provider = "battlenet"
            )
            every { authenticationService.getBattlenetAuthUrl(any()) } returns expected

            // When
            val result = controller.getBattlenetAuthUrl(state = null)

            // Then
            result.url shouldBe expected.url
            result.provider shouldBe "battlenet"
        }

        @Test
        fun `should exchange Battlenet code for tokens`() {
            // Given
            val request = OAuth2CallbackRequest(code = "auth-code-456")
            val expected = TokenResponse(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 900
            )
            every { authenticationService.authenticateWithBattlenet(request.code) } returns expected

            // When
            val result = controller.battlenetCallback(request)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.accessToken shouldBe "access-token"
        }
    }

    @Nested
    inner class CurrentUser {

        @Test
        fun `should return current user profile`() {
            // Given
            val expected = UserProfileResponse(
                id = 1L,
                discordId = "123456789012345678",
                battlenetId = null,
                username = "testuser",
                email = "test@example.com",
                avatarUrl = "https://avatar.png",
                role = "RAIDER",
                guildId = "test-guild",
                createdAt = Instant.now(),
                lastLogin = Instant.now()
            )
            every { authenticationService.getCurrentUser("valid-token") } returns expected

            // When
            val result = controller.getCurrentUser("Bearer valid-token")

            // Then
            result.id shouldBe 1L
            result.username shouldBe "testuser"
        }

        @Test
        fun `should reject invalid authorization header`() {
            // When & Then
            shouldThrow<IllegalArgumentException> {
                controller.getCurrentUser("InvalidHeader token")
            }
        }
    }

    @Nested
    inner class RefreshToken {

        @Test
        fun `should refresh access token`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "old-refresh-token")
            val expected = TokenResponse(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                expiresIn = 900
            )
            every { authenticationService.refreshAccessToken(request.refreshToken) } returns expected

            // When
            val result = controller.refreshToken(request)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.accessToken shouldBe "new-access-token"
        }

        @Test
        fun `should propagate invalid refresh token exception`() {
            // Given
            val request = RefreshTokenRequest(refreshToken = "invalid-token")
            every { authenticationService.refreshAccessToken(any()) } throws
                InvalidRefreshTokenException()

            // When & Then
            shouldThrow<InvalidRefreshTokenException> {
                controller.refreshToken(request)
            }
        }
    }

    @Nested
    inner class Logout {

        @Test
        fun `should logout user`() {
            // Given
            val userId = UserId(1L)
            every { authenticationService.validateToken("valid-token") } returns userId
            every { authenticationService.logout(userId) } returns LogoutResponse(success = true)

            // When
            val result = controller.logout("Bearer valid-token")

            // Then
            result.success shouldBe true
            verify(exactly = 1) { authenticationService.logout(userId) }
        }

        @Test
        fun `should reject invalid token on logout`() {
            // Given
            every { authenticationService.validateToken(any()) } returns null

            // When & Then
            shouldThrow<IllegalArgumentException> {
                controller.logout("Bearer invalid-token")
            }
        }
    }
}
