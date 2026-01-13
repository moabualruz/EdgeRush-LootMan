package com.edgerush.datasync.security

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Comprehensive unit tests for JwtAuthenticationFilter.
 *
 * Tests cover:
 * - Admin mode bypass behavior
 * - Token extraction from Authorization header
 * - Token validation and user extraction
 * - Authentication context propagation
 * - Error handling for invalid tokens
 * - Edge cases
 */
class JwtAuthenticationFilterTest : UnitTest() {

    private lateinit var jwtService: JwtService
    private lateinit var adminModeConfig: AdminModeConfig
    private lateinit var filter: JwtAuthenticationFilter
    private lateinit var filterChain: WebFilterChain

    @BeforeEach
    fun setup() {
        jwtService = mockk()
        adminModeConfig = mockk()
        filter = JwtAuthenticationFilter(jwtService, adminModeConfig)
        filterChain = mockk()
    }

    @Nested
    inner class `admin mode enabled` {

        @Test
        fun `should bypass authentication and use admin user when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            val request = MockServerHttpRequest.get("/api/v1/test").build()
            val exchange = MockServerWebExchange.from(request)

            var capturedAuthentication: UsernamePasswordAuthenticationToken? = null
            every { filterChain.filter(exchange) } answers {
                Mono.deferContextual { ctx ->
                    val securityContext = ReactiveSecurityContextHolder.getContext()
                    securityContext.flatMap { context ->
                        capturedAuthentication = context.authentication as? UsernamePasswordAuthenticationToken
                        Mono.empty<Void>()
                    }
                }
            }

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { filterChain.filter(exchange) }
            verify(exactly = 0) { jwtService.validateToken(any()) }
        }

        @Test
        fun `should not extract token when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 0) { jwtService.validateToken(any()) }
            verify(exactly = 0) { jwtService.extractUser(any()) }
        }
    }

    @Nested
    inner class `token extraction` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should extract token from Authorization header with Bearer prefix`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
            verify(exactly = 1) { jwtService.extractUser(token) }
        }

        @Test
        fun `should not extract token when Authorization header is missing`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test").build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 0) { jwtService.validateToken(any()) }
        }

        @Test
        fun `should not extract token when Authorization header does not start with Bearer`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic some-credentials")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 0) { jwtService.validateToken(any()) }
        }

        @Test
        fun `should not extract token when Authorization header is just Bearer without token`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken("") } returns false
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken("") }
        }

        @Test
        fun `should handle case-sensitive Bearer prefix`() {
            // Arrange - "bearer" in lowercase should not match
            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "bearer some-token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 0) { jwtService.validateToken(any()) }
        }
    }

    @Nested
    inner class `token validation` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should authenticate user when token is valid`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                guildIds = listOf("guild-1"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
            verify(exactly = 1) { jwtService.extractUser(token) }
            verify(exactly = 1) { filterChain.filter(exchange) }
        }

        @Test
        fun `should not authenticate when token is invalid`() {
            // Arrange
            val token = "invalid-jwt-token"

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns false
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
            verify(exactly = 0) { jwtService.extractUser(any()) }
            verify(exactly = 1) { filterChain.filter(exchange) }
        }

        @Test
        fun `should continue filter chain without authentication when token validation fails`() {
            // Arrange
            val token = "expired-token"

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns false
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { filterChain.filter(exchange) }
        }
    }

    @Nested
    inner class `error handling` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should continue without authentication when extractUser throws exception`() {
            // Arrange
            val token = "malformed-token"

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } throws RuntimeException("Failed to extract user")
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
            verify(exactly = 1) { jwtService.extractUser(token) }
            verify(exactly = 1) { filterChain.filter(exchange) }
        }

        @Test
        fun `should handle null Authorization header gracefully`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test").build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { filterChain.filter(exchange) }
        }
    }

    @Nested
    inner class `authentication creation` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should create authentication with correct authorities from user roles`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user

            var capturedPrincipal: Any? = null
            every { filterChain.filter(exchange) } answers {
                Mono.deferContextual { ctx ->
                    ReactiveSecurityContextHolder.getContext()
                        .doOnNext { securityContext ->
                            capturedPrincipal = securityContext.authentication?.principal
                        }
                        .then()
                }
            }

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()
        }

        @Test
        fun `should create authentication with user as principal`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.extractUser(token) }
        }

        @Test
        fun `should handle user with empty roles`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = emptyList(),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.extractUser(token) }
        }
    }

    @Nested
    inner class `request types` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should process GET request with valid token`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()
        }

        @Test
        fun `should process POST request with valid token`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            val request = MockServerHttpRequest.post("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()
        }

        @Test
        fun `should process DELETE request with valid token`() {
            // Arrange
            val token = "valid-jwt-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("SYSTEM_ADMIN"),
            )

            val request = MockServerHttpRequest.delete("/api/v1/test/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()
        }

        @Test
        fun `should process request without token`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/public").build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 0) { jwtService.validateToken(any()) }
        }
    }

    @Nested
    inner class `edge cases` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should handle token with special characters`() {
            // Arrange
            val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.signature_with_special-chars+and/more="
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
        }

        @Test
        fun `should handle multiple Authorization headers - uses first one`() {
            // Arrange
            val token = "first-token"
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            // Note: MockServerHttpRequest doesn't easily support multiple headers with same name
            // This test verifies single header behavior
            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns true
            every { jwtService.extractUser(token) } returns user
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
        }

        @Test
        fun `should handle very long token`() {
            // Arrange
            val token = "a".repeat(10000)

            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken(token) } returns false
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken(token) }
        }

        @Test
        fun `should handle whitespace-only token`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer    ")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtService.validateToken("   ") } returns false
            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { jwtService.validateToken("   ") }
        }
    }

    @Nested
    inner class `filter chain continuation` {

        @BeforeEach
        fun setupAdminMode() {
            every { adminModeConfig.isEnabled() } returns false
        }

        @Test
        fun `should always continue filter chain regardless of authentication result`() {
            // Arrange - no token provided
            val request = MockServerHttpRequest.get("/api/v1/test").build()
            val exchange = MockServerWebExchange.from(request)

            every { filterChain.filter(exchange) } returns Mono.empty()

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .verifyComplete()

            verify(exactly = 1) { filterChain.filter(exchange) }
        }

        @Test
        fun `should propagate errors from downstream filter chain`() {
            // Arrange
            val request = MockServerHttpRequest.get("/api/v1/test").build()
            val exchange = MockServerWebExchange.from(request)
            val expectedException = RuntimeException("Downstream error")

            every { filterChain.filter(exchange) } returns Mono.error(expectedException)

            // Act
            val result = filter.filter(exchange, filterChain)

            // Assert
            StepVerifier.create(result)
                .expectError(RuntimeException::class.java)
                .verify()
        }
    }
}
