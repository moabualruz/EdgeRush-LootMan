package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for WoWAuditProperties.
 *
 * Tests data class behavior and property handling.
 */
class WoWAuditPropertiesTest : UnitTest() {

    @Test
    fun `should create properties with all values`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/US/Illidan/TestGuild/profile",
            apiKey = "test-api-key-123",
        )

        // Assert
        properties.baseUrl shouldBe "https://api.wowaudit.com"
        properties.guildProfileUri shouldBe "https://wowaudit.com/US/Illidan/TestGuild/profile"
        properties.apiKey shouldBe "test-api-key-123"
    }

    @Test
    fun `should allow null guildProfileUri`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = null,
            apiKey = "test-api-key-123",
        )

        // Assert
        properties.guildProfileUri shouldBe null
    }

    @Test
    fun `should allow null apiKey`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/US/Illidan/TestGuild/profile",
            apiKey = null,
        )

        // Assert
        properties.apiKey shouldBe null
    }

    @Test
    fun `should allow all optional fields to be null`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://wowaudit.com",
            guildProfileUri = null,
            apiKey = null,
        )

        // Assert
        properties.baseUrl shouldBe "https://wowaudit.com"
        properties.guildProfileUri shouldBe null
        properties.apiKey shouldBe null
    }

    @Test
    fun `should support copy with modifications`() {
        // Arrange
        val original = WoWAuditProperties(
            baseUrl = "https://original.com",
            guildProfileUri = "https://original.com/profile",
            apiKey = "original-key",
        )

        // Act
        val copied = original.copy(baseUrl = "https://new.com")

        // Assert
        copied.baseUrl shouldBe "https://new.com"
        copied.guildProfileUri shouldBe "https://original.com/profile"
        copied.apiKey shouldBe "original-key"
    }

    @Test
    fun `should support equality comparison`() {
        // Arrange
        val properties1 = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/profile",
            apiKey = "key-123",
        )
        val properties2 = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/profile",
            apiKey = "key-123",
        )

        // Assert
        properties1 shouldBe properties2
    }

    @Test
    fun `should work with EU guild profile URIs`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/EU/Kazzak/Elite-Raiders/profile",
            apiKey = "eu-api-key",
        )

        // Assert
        properties.guildProfileUri shouldBe "https://wowaudit.com/EU/Kazzak/Elite-Raiders/profile"
    }

    @Test
    fun `should work with guild names containing special characters`() {
        // Arrange & Act
        val properties = WoWAuditProperties(
            baseUrl = "https://api.wowaudit.com",
            guildProfileUri = "https://wowaudit.com/US/Area-52/Test%20Guild/profile",
            apiKey = "test-key",
        )

        // Assert
        properties.guildProfileUri shouldBe "https://wowaudit.com/US/Area-52/Test%20Guild/profile"
    }
}
