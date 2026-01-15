package com.edgerush.datasync.security

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for AdminModeConfig.
 *
 * Tests configuration behavior without requiring Spring context.
 */
class AdminModeConfigTest : UnitTest() {
    @Test
    fun `should be disabled by default`() {
        // Arrange & Act
        val config = AdminModeConfig()

        // Assert
        config.enabled shouldBe false
        config.isEnabled() shouldBe false
    }

    @Nested
    inner class `logWarning PostConstruct` {
        @Test
        fun `should execute logWarning without error when admin mode is enabled`() {
            // Arrange
            val config = AdminModeConfig(enabled = true)

            // Act - should log warning messages but not throw
            config.logWarning()

            // Assert - no exception means success
            config.isEnabled() shouldBe true
        }

        @Test
        fun `should execute logWarning without error when admin mode is disabled`() {
            // Arrange
            val config = AdminModeConfig(enabled = false)

            // Act - should not log anything but still not throw
            config.logWarning()

            // Assert - no exception means success
            config.isEnabled() shouldBe false
        }

        @Test
        fun `should be callable multiple times without error`() {
            // Arrange
            val config = AdminModeConfig(enabled = true)

            // Act - call multiple times
            config.logWarning()
            config.logWarning()
            config.logWarning()

            // Assert - no exception means success
            config.isEnabled() shouldBe true
        }
    }

    @Test
    fun `isEnabled should return true when enabled is true`() {
        // Arrange
        val config = AdminModeConfig(enabled = true)

        // Act & Assert
        config.isEnabled() shouldBe true
    }

    @Test
    fun `isEnabled should return false when enabled is false`() {
        // Arrange
        val config = AdminModeConfig(enabled = false)

        // Act & Assert
        config.isEnabled() shouldBe false
    }

    @Test
    fun `should allow modifying enabled property`() {
        // Arrange
        val config = AdminModeConfig()

        // Act
        config.enabled = true

        // Assert
        config.isEnabled() shouldBe true
    }

    @Test
    fun `should allow toggling enabled property`() {
        // Arrange
        val config = AdminModeConfig(enabled = true)

        // Act
        config.enabled = false

        // Assert
        config.isEnabled() shouldBe false
    }

    @Test
    fun `should support copy with modifications`() {
        // Arrange
        val original = AdminModeConfig(enabled = false)

        // Act
        val copied = original.copy(enabled = true)

        // Assert
        copied.isEnabled() shouldBe true
        original.isEnabled() shouldBe false
    }

    @Test
    fun `should support equality comparison`() {
        // Arrange
        val config1 = AdminModeConfig(enabled = true)
        val config2 = AdminModeConfig(enabled = true)

        // Assert
        config1 shouldBe config2
    }

    @Test
    fun `enabled field should be mutable for Spring property binding`() {
        // Arrange
        val config = AdminModeConfig()

        // Act - simulate Spring property binding
        config.enabled = true

        // Assert
        config.enabled shouldBe true
        config.isEnabled() shouldBe true
    }
}
