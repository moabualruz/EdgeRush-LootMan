package com.edgerush.datasync.config

import com.edgerush.datasync.security.AdminModeConfig
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Unit tests for RateLimitFilter.
 *
 * Tests rate limiting behavior for read and write operations,
 * including bypassing for admin mode and disabled states.
 */
class RateLimitFilterTest : UnitTest() {

    @MockK
    private lateinit var adminModeConfig: AdminModeConfig

    @MockK(relaxed = true)
    private lateinit var exchange: ServerWebExchange

    @MockK(relaxed = true)
    private lateinit var request: ServerHttpRequest

    @MockK(relaxed = true)
    private lateinit var response: ServerHttpResponse

    @MockK
    private lateinit var chain: WebFilterChain

    @MockK(relaxed = true)
    private lateinit var headers: HttpHeaders

    private lateinit var rateLimitProperties: RateLimitProperties

    @BeforeEach
    fun setUp() {
        every { exchange.request } returns request
        every { exchange.response } returns response
        every { response.headers } returns headers
        every { chain.filter(exchange) } returns Mono.empty()
        every { response.setComplete() } returns Mono.empty()
    }

    @Nested
    inner class WhenRateLimitingDisabled {

        @Test
        fun `should pass through when rate limiting is disabled`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(enabled = false)
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.GET
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should not check rate limiter when disabled`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(enabled = false)
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.POST
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 0) { response.statusCode = any() }
        }
    }

    @Nested
    inner class WhenAdminModeEnabled {

        @Test
        fun `should pass through when admin mode is enabled`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(enabled = true)
            every { adminModeConfig.isEnabled() } returns true
            every { request.method } returns HttpMethod.GET
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should bypass rate limiting for write operations when admin mode enabled`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(enabled = true)
            every { adminModeConfig.isEnabled() } returns true
            every { request.method } returns HttpMethod.POST
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 0) { response.statusCode = any() }
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    @Nested
    inner class WriteOperationDetection {

        @Test
        fun `should identify POST as write operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 1000.0, // High limit to ensure it passes
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.POST
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert - if it used write limiter and passed, chain.filter was called
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify PUT as write operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.PUT
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify DELETE as write operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.DELETE
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify PATCH as write operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.PATCH
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify GET as read operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.GET
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify HEAD as read operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.HEAD
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should identify OPTIONS as read operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.OPTIONS
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    @Nested
    inner class RateLimitExceeded {

        @Test
        fun `should complete response when read rate limit exceeded`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 0.001, // Very low limit to ensure it fails on second request
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.GET
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act - make multiple requests to exceed limit
            filter.filter(exchange, chain).block()
            filter.filter(exchange, chain).block()

            // Assert - at least one request should have been rate limited (setComplete called)
            verify(atLeast = 1) { response.setComplete() }
        }

        @Test
        fun `should add Retry-After header when rate limit exceeded`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 0.001,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.GET
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act - exhaust the rate limit
            filter.filter(exchange, chain).block()
            filter.filter(exchange, chain).block()

            // Assert - verify Retry-After header was added at least once
            verify(atLeast = 1) { headers.add("Retry-After", "1") }
        }

        @Test
        fun `should complete response when write rate limit exceeded`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 0.001,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.POST
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act - make multiple requests to exceed limit
            filter.filter(exchange, chain).block()
            filter.filter(exchange, chain).block()

            // Assert - at least one request should have been rate limited (setComplete called)
            verify(atLeast = 1) { response.setComplete() }
        }
    }

    @Nested
    inner class NullMethodHandling {

        @Test
        fun `should treat null method as read operation`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 1000.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            every { request.method } returns null
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    @Nested
    inner class RateLimiterInitialization {

        @Test
        fun `should create filter with custom read rate`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 500.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.GET

            // Act
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)
            filter.filter(exchange, chain).block()

            // Assert - should successfully create and use the filter
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should create filter with custom write rate`() {
            // Arrange
            rateLimitProperties = RateLimitProperties(
                enabled = true,
                writeRequestsPerSecond = 50.0,
            )
            every { adminModeConfig.isEnabled() } returns false
            every { request.method } returns HttpMethod.POST

            // Act
            val filter = RateLimitFilter(rateLimitProperties, adminModeConfig)
            filter.filter(exchange, chain).block()

            // Assert - should successfully create and use the filter
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }
}
