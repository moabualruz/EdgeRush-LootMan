package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.DeprecatedEndpoint
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.URI

/**
 * Unit tests for DeprecationHeaderFilter.
 *
 * Tests that the filter adds appropriate Deprecation and Sunset headers
 * to responses for endpoints marked with @DeprecatedEndpoint annotation.
 */
class DeprecationHeaderFilterTest : UnitTest() {

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

    @MockK(relaxed = true)
    private lateinit var handlerMapping: RequestMappingHandlerMapping

    private lateinit var filter: DeprecationHeaderFilter

    @BeforeEach
    fun setUp() {
        every { exchange.request } returns request
        every { exchange.response } returns response
        every { response.headers } returns headers
        every { chain.filter(exchange) } returns Mono.empty()
        every { request.uri } returns URI.create("/api/v1/test")

        filter = DeprecationHeaderFilter(handlerMapping)
    }

    @Nested
    inner class WhenEndpointNotDeprecated {

        @Test
        fun `should not add deprecation headers when endpoint is not deprecated`() {
            // Arrange
            every { handlerMapping.getHandler(exchange) } returns Mono.empty()

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 0) { headers.add("Deprecation", any()) }
            verify(exactly = 0) { headers.add("Sunset", any()) }
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should pass through to chain when no handler found`() {
            // Arrange
            every { handlerMapping.getHandler(exchange) } returns Mono.empty()

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    @Nested
    inner class WhenEndpointIsDeprecated {

        @Test
        fun `should add Deprecation header with date when endpoint is deprecated`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "",
                replacement = "",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { headers.add("Deprecation", "date=\"2026-01-01\"") }
            verify(exactly = 1) { chain.filter(exchange) }
        }

        @Test
        fun `should add Sunset header when sunset date is specified`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "2026-06-01",
                replacement = "",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { headers.add("Sunset", "2026-06-01") }
        }

        @Test
        fun `should not add Sunset header when sunset date is empty`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "",
                replacement = "",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 0) { headers.add("Sunset", any()) }
        }

        @Test
        fun `should add Link header when replacement endpoint is specified`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "",
                replacement = "/api/v2/new-endpoint",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) {
                headers.add("Link", "</api/v2/new-endpoint>; rel=\"successor-version\"")
            }
        }

        @Test
        fun `should not add Link header when replacement is empty`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "",
                replacement = "",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 0) { headers.add("Link", any()) }
        }

        @Test
        fun `should add all headers when fully configured`() {
            // Arrange
            val handler = createDeprecatedHandlerMethod(
                since = "2026-01-01",
                sunset = "2026-06-01",
                replacement = "/api/v2/new-endpoint",
            )
            every { handlerMapping.getHandler(exchange) } returns Mono.just(handler)

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { headers.add("Deprecation", "date=\"2026-01-01\"") }
            verify(exactly = 1) { headers.add("Sunset", "2026-06-01") }
            verify(exactly = 1) {
                headers.add("Link", "</api/v2/new-endpoint>; rel=\"successor-version\"")
            }
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `should continue chain when handler mapping throws exception`() {
            // Arrange
            every { handlerMapping.getHandler(exchange) } returns Mono.error(RuntimeException("Error"))

            // Act
            filter.filter(exchange, chain).block()

            // Assert
            verify(exactly = 1) { chain.filter(exchange) }
        }
    }

    // Helper method to create a mock HandlerMethod with DeprecatedEndpoint annotation
    private fun createDeprecatedHandlerMethod(
        since: String,
        sunset: String,
        replacement: String,
    ): HandlerMethod {
        // Find the appropriate test method based on parameters
        val method = when {
            sunset.isNotBlank() && replacement.isNotBlank() -> {
                TestDeprecatedController::class.java.getMethod("fullyConfiguredEndpoint")
            }
            sunset.isNotBlank() -> {
                TestDeprecatedController::class.java.getMethod("withSunsetEndpoint")
            }
            replacement.isNotBlank() -> {
                TestDeprecatedController::class.java.getMethod("withReplacementEndpoint")
            }
            else -> {
                TestDeprecatedController::class.java.getMethod("basicDeprecatedEndpoint")
            }
        }
        return HandlerMethod(TestDeprecatedController(), method)
    }

    // Test controller with various deprecation configurations
    private class TestDeprecatedController {
        @DeprecatedEndpoint(since = "2026-01-01")
        fun basicDeprecatedEndpoint(): String = "deprecated"

        @DeprecatedEndpoint(since = "2026-01-01", sunset = "2026-06-01")
        fun withSunsetEndpoint(): String = "deprecated with sunset"

        @DeprecatedEndpoint(since = "2026-01-01", replacement = "/api/v2/new-endpoint")
        fun withReplacementEndpoint(): String = "deprecated with replacement"

        @DeprecatedEndpoint(
            since = "2026-01-01",
            sunset = "2026-06-01",
            replacement = "/api/v2/new-endpoint",
        )
        fun fullyConfiguredEndpoint(): String = "fully configured"
    }
}
