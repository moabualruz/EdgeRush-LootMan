package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.util.function.Consumer

/**
 * Unit tests for WebClientConfig.
 *
 * Tests WebClient bean configuration including base URL setup,
 * authentication headers, timeout configuration, and retry strategies.
 */
class WebClientConfigTest : UnitTest() {
    @MockK(relaxed = true)
    private lateinit var webClientBuilder: WebClient.Builder

    @MockK(relaxed = true)
    private lateinit var webClient: WebClient

    private lateinit var webClientConfig: WebClientConfig
    private lateinit var httpClientsProperties: HttpClientsProperties

    @BeforeEach
    fun setUp() {
        httpClientsProperties = createHttpClientsProperties()
        webClientConfig = WebClientConfig(httpClientsProperties)
        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.defaultHeaders(any()) } returns webClientBuilder
        every { webClientBuilder.clientConnector(any<ReactorClientHttpConnector>()) } returns webClientBuilder
        every { webClientBuilder.filter(any<ExchangeFilterFunction>()) } returns webClientBuilder
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

        @Test
        fun `should configure client connector with timeouts`() {
            // Arrange
            val properties = createSyncProperties()

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            verify(exactly = 1) { webClientBuilder.clientConnector(any<ReactorClientHttpConnector>()) }
        }

        @Test
        fun `should configure retry filter`() {
            // Arrange
            val properties = createSyncProperties()

            // Act
            webClientConfig.wowauditWebClient(webClientBuilder, properties)

            // Assert
            verify(exactly = 1) { webClientBuilder.filter(any<ExchangeFilterFunction>()) }
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
    inner class HttpClientsPropertiesTests {
        @Test
        fun `should use configured timeout values`() {
            // Arrange
            val customProperties = HttpClientsProperties(
                connectTimeoutMs = 3000,
                readTimeoutMs = 6000,
                writeTimeoutMs = 6000,
                maxRetries = 5,
                retryBackoffMs = 1000,
            )
            val config = WebClientConfig(customProperties)
            val syncProperties = createSyncProperties()

            // Act
            config.wowauditWebClient(webClientBuilder, syncProperties)

            // Assert - verify that client connector is configured (timeouts are internal)
            verify(exactly = 1) { webClientBuilder.clientConnector(any<ReactorClientHttpConnector>()) }
        }

        @Test
        fun `should use default timeout values`() {
            // Arrange
            val defaultProperties = createHttpClientsProperties()

            // Assert
            defaultProperties.connectTimeoutMs shouldBe 5000
            defaultProperties.readTimeoutMs shouldBe 10000
            defaultProperties.writeTimeoutMs shouldBe 10000
            defaultProperties.maxRetries shouldBe 3
            defaultProperties.retryBackoffMs shouldBe 500
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

    @Nested
    inner class ServerErrorExceptionTests {
        @Test
        fun `ServerErrorException should contain status code and message`() {
            // Arrange & Act
            val exception = ServerErrorException(
                statusCode = mockk { every { value() } returns 503 },
                message = "Service Unavailable",
            )

            // Assert
            exception.message shouldBe "Service Unavailable"
            exception.statusCode.value() shouldBe 503
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

    // Helper function to create HttpClientsProperties for testing
    private fun createHttpClientsProperties(
        connectTimeoutMs: Int = 5000,
        readTimeoutMs: Int = 10000,
        writeTimeoutMs: Int = 10000,
        maxRetries: Int = 3,
        retryBackoffMs: Long = 500,
    ): HttpClientsProperties =
        HttpClientsProperties(
            connectTimeoutMs = connectTimeoutMs,
            readTimeoutMs = readTimeoutMs,
            writeTimeoutMs = writeTimeoutMs,
            maxRetries = maxRetries,
            retryBackoffMs = retryBackoffMs,
        )
}
