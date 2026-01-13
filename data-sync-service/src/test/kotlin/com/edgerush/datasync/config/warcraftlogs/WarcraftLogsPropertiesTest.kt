package com.edgerush.datasync.config.warcraftlogs

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

/**
 * Unit tests for WarcraftLogsProperties.
 *
 * Tests default values, validation, and configuration requirements.
 */
class WarcraftLogsPropertiesTest : UnitTest() {

    @Test
    fun `should have correct default values when disabled`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties()

        // Assert
        properties.enabled shouldBe false
        properties.clientId shouldBe ""
        properties.clientSecret shouldBe ""
        properties.baseUrl shouldBe "https://www.warcraftlogs.com/api/v2"
        properties.tokenUrl shouldBe "https://www.warcraftlogs.com/oauth/token"
        properties.maxRetries shouldBe 3
        properties.retryDelayMs shouldBe 1000
        properties.maxConcurrentRequests shouldBe 5
        properties.requestTimeoutSeconds shouldBe 30
    }

    @Test
    fun `should allow creation with valid credentials when enabled`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-client-secret",
        )

        // Assert
        properties.enabled shouldBe true
        properties.clientId shouldBe "valid-client-id"
        properties.clientSecret shouldBe "valid-client-secret"
    }

    @Test
    fun `should throw exception when enabled with blank clientId`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "",
                clientSecret = "valid-secret",
            )
        }
        exception.message shouldContain "client ID is required"
    }

    @Test
    fun `should throw exception when enabled with blank clientSecret`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "valid-client-id",
                clientSecret = "",
            )
        }
        exception.message shouldContain "client secret is required"
    }

    @Test
    fun `should throw exception when enabled with whitespace-only clientId`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "   ",
                clientSecret = "valid-secret",
            )
        }
        exception.message shouldContain "client ID is required"
    }

    @Test
    fun `should throw exception when maxRetries is less than 1`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "valid-client-id",
                clientSecret = "valid-secret",
                maxRetries = 0,
            )
        }
        exception.message shouldContain "Max retries must be at least 1"
    }

    @Test
    fun `should throw exception when retryDelayMs is less than minimum`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "valid-client-id",
                clientSecret = "valid-secret",
                retryDelayMs = 50,
            )
        }
        exception.message shouldContain "Retry delay must be at least"
    }

    @Test
    fun `should throw exception when maxConcurrentRequests is less than 1`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "valid-client-id",
                clientSecret = "valid-secret",
                maxConcurrentRequests = 0,
            )
        }
        exception.message shouldContain "Max concurrent requests must be at least 1"
    }

    @Test
    fun `should throw exception when requestTimeoutSeconds is less than 1`() {
        // Arrange & Act & Assert
        val exception = shouldThrow<IllegalArgumentException> {
            WarcraftLogsProperties(
                enabled = true,
                clientId = "valid-client-id",
                clientSecret = "valid-secret",
                requestTimeoutSeconds = 0,
            )
        }
        exception.message shouldContain "Request timeout must be at least 1 second"
    }

    @Test
    fun `should not validate credentials when disabled`() {
        // Arrange & Act - should not throw
        val properties = WarcraftLogsProperties(
            enabled = false,
            clientId = "",
            clientSecret = "",
        )

        // Assert
        properties.enabled shouldBe false
    }

    @Test
    fun `should allow custom baseUrl`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-secret",
            baseUrl = "https://custom.warcraftlogs.com/api/v2",
        )

        // Assert
        properties.baseUrl shouldBe "https://custom.warcraftlogs.com/api/v2"
    }

    @Test
    fun `should allow custom tokenUrl`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-secret",
            tokenUrl = "https://custom.warcraftlogs.com/oauth/token",
        )

        // Assert
        properties.tokenUrl shouldBe "https://custom.warcraftlogs.com/oauth/token"
    }

    @Test
    fun `should accept minimum valid retryDelayMs`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-secret",
            retryDelayMs = 100,
        )

        // Assert
        properties.retryDelayMs shouldBe 100
    }

    @Test
    fun `should allow high maxRetries value`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-secret",
            maxRetries = 10,
        )

        // Assert
        properties.maxRetries shouldBe 10
    }

    @Test
    fun `should allow high maxConcurrentRequests value`() {
        // Arrange & Act
        val properties = WarcraftLogsProperties(
            enabled = true,
            clientId = "valid-client-id",
            clientSecret = "valid-secret",
            maxConcurrentRequests = 20,
        )

        // Assert
        properties.maxConcurrentRequests shouldBe 20
    }

    @Test
    fun `should support copy with modifications`() {
        // Arrange
        val original = WarcraftLogsProperties(
            enabled = true,
            clientId = "original-id",
            clientSecret = "original-secret",
        )

        // Act
        val copied = original.copy(clientId = "new-id")

        // Assert
        copied.clientId shouldBe "new-id"
        copied.clientSecret shouldBe "original-secret"
    }
}
