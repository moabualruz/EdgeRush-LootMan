package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.util.function.Consumer

/**
 * Unit tests for WebClientConfig.
 *
 * Tests WebClient bean configuration including base URL setup,
 * authentication headers, and memory settings.
 */
class WebClientConfigTest : UnitTest() {
    @MockK(relaxed = true)
    private lateinit var webClientBuilder: WebClient.Builder

    @MockK(relaxed = true)
    private lateinit var webClient: WebClient

    private lateinit var webClientConfig: WebClientConfig

    @BeforeEach
    fun setUp() {
        webClientConfig = WebClientConfig()
        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.defaultHeaders(any()) } returns webClientBuilder
        every { webClientBuilder.exchangeStrategies(any<ExchangeStrategies>()) } returns webClientBuilder
        every { webClientBuilder.build() } returns webClient
    }

    @Nested
    inner class WowauditWebClientBean {
        @Test
        fun `should configure base URL from properties`() {
            // Arrange
            val properties = createSyncProperties(baseUrl = "https://api.wowaudit.com")
            val baseUrlSlot = slot<String>()
            every { webClientBuilder.baseUrl(capture(baseUrlSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            baseUrlSlot.captured shouldBe "https://api.wowaudit.com"
        }

        @Test
        fun `should configure custom base URL from properties`() {
            // Arrange
            val properties = createSyncProperties(baseUrl = "https://custom.wowaudit.com/api")
            val baseUrlSlot = slot<String>()
            every { webClientBuilder.baseUrl(capture(baseUrlSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            baseUrlSlot.captured shouldBe "https://custom.wowaudit.com/api"
        }

        @Test
        fun `should return configured WebClient`() {
            // Arrange
            val properties = createSyncProperties()

            // Act
            val result = webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            result shouldBe webClient
        }

        @Test
        fun `should build WebClient`() {
            // Arrange
            val properties = createSyncProperties()

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            verify(exactly = 1) { webClientBuilder.build() }
        }
    }

    @Nested
    inner class DefaultHeaders {
        @Test
        fun `should set bearer auth when API key is provided`() {
            // Arrange
            val properties = createSyncProperties(apiKey = "test-api-key-123")
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.AUTHORIZATION) shouldBe "Bearer test-api-key-123"
        }

        @Test
        fun `should not set bearer auth when API key is null`() {
            // Arrange
            val properties = createSyncProperties(apiKey = null)
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.AUTHORIZATION) shouldBe null
        }

        @Test
        fun `should not set bearer auth when API key is blank`() {
            // Arrange
            val properties = createSyncProperties(apiKey = "   ")
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.AUTHORIZATION) shouldBe null
        }

        @Test
        fun `should not set bearer auth when API key is empty`() {
            // Arrange
            val properties = createSyncProperties(apiKey = "")
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.AUTHORIZATION) shouldBe null
        }

        @Test
        fun `should set User-Agent header`() {
            // Arrange
            val properties = createSyncProperties()
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.USER_AGENT).shouldNotBeNull()
            headers.getFirst(HttpHeaders.USER_AGENT)!! shouldContain "EdgeRushLootMan"
        }

        @Test
        fun `should set Accept header to application json`() {
            // Arrange
            val properties = createSyncProperties()
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.ACCEPT) shouldBe "application/json"
        }

        @Test
        fun `should not overwrite existing User-Agent header`() {
            // Arrange
            val properties = createSyncProperties()
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headers.set(HttpHeaders.USER_AGENT, "CustomAgent/1.0")
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.USER_AGENT) shouldBe "CustomAgent/1.0"
        }

        @Test
        fun `should not overwrite existing Accept header`() {
            // Arrange
            val properties = createSyncProperties()
            val headersConsumerSlot = slot<Consumer<HttpHeaders>>()
            every { webClientBuilder.defaultHeaders(capture(headersConsumerSlot)) } returns webClientBuilder

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            val headers = HttpHeaders()
            headers.set(HttpHeaders.ACCEPT, "text/html")
            headersConsumerSlot.captured.accept(headers)
            headers.getFirst(HttpHeaders.ACCEPT) shouldBe "text/html"
        }
    }

    @Nested
    inner class ExchangeStrategiesConfiguration {
        @Test
        fun `should configure exchange strategies`() {
            // Arrange
            val properties = createSyncProperties()

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            verify(exactly = 1) { webClientBuilder.exchangeStrategies(any<ExchangeStrategies>()) }
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle properties with all nullable fields as null`() {
            // Arrange
            val wowAuditProperties =
                WoWAuditProperties(
                    baseUrl = "https://wowaudit.com",
                    guildProfileUri = null,
                    apiKey = null,
                )
            val properties =
                SyncProperties(
                    cron = "0 0 4 * * *",
                    runOnStartup = false,
                    wowaudit = wowAuditProperties,
                )

            // Act & Assert - should not throw
            webClientConfig.wowauditWebClient(webClientBuilder, properties)
            verify(exactly = 1) { webClientBuilder.build() }
        }

        @Test
        fun `should work with minimal configuration`() {
            // Arrange
            val properties =
                createSyncProperties(
                    baseUrl = "https://minimal.test",
                    apiKey = null,
                )

            // Act
            val result = webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            result.shouldNotBeNull()
        }
    }

    // Helper function to create SyncProperties for testing
    private fun createSyncProperties(
        baseUrl: String = "https://wowaudit.com",
        guildProfileUri: String? = "https://wowaudit.com/US/Illidan/TestGuild/profile",
        apiKey: String? = "test-api-key",
        cron: String = "0 0 4 * * *",
        runOnStartup: Boolean = false,
    ): SyncProperties {
        val wowAuditProperties =
            WoWAuditProperties(
                baseUrl = baseUrl,
                guildProfileUri = guildProfileUri,
                apiKey = apiKey,
            )
        return SyncProperties(
            cron = cron,
            runOnStartup = runOnStartup,
            wowaudit = wowAuditProperties,
        )
    }
}
