package com.edgerush.lootman.domain.application.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApplicationId value object.
 */
class ApplicationIdTest : UnitTest() {

    @Nested
    inner class CreationTests {

        @Test
        fun `should create valid application ID with non-blank value`() {
            // Arrange & Act
            val applicationId = ApplicationId("app-123")

            // Assert
            applicationId.value shouldBe "app-123"
        }

        @Test
        fun `should create application ID with UUID format`() {
            // Arrange & Act
            val applicationId = ApplicationId("550e8400-e29b-41d4-a716-446655440000")

            // Assert
            applicationId.value shouldBe "550e8400-e29b-41d4-a716-446655440000"
        }
    }

    @Nested
    inner class ValidationTests {

        @Test
        fun `should throw exception when value is blank`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ApplicationId("")
            }
            exception.message shouldContain "blank"
        }

        @Test
        fun `should throw exception when value is whitespace only`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ApplicationId("   ")
            }
            exception.message shouldContain "blank"
        }
    }

    @Nested
    inner class GenerationTests {

        @Test
        fun `should generate unique application ID`() {
            // Arrange & Act
            val id1 = ApplicationId.generate()
            val id2 = ApplicationId.generate()

            // Assert
            id1 shouldNotBe id2
        }

        @Test
        fun `should generate non-blank application ID`() {
            // Arrange & Act
            val applicationId = ApplicationId.generate()

            // Assert
            applicationId.value.isNotBlank() shouldBe true
        }

        @Test
        fun `should generate UUID format application ID`() {
            // Arrange & Act
            val applicationId = ApplicationId.generate()

            // Assert
            applicationId.value.length shouldBe 36 // UUID format
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val id1 = ApplicationId("app-123")
            val id2 = ApplicationId("app-123")

            // Assert
            id1 shouldBe id2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val id1 = ApplicationId("app-123")
            val id2 = ApplicationId("app-123")

            // Assert
            id1.hashCode() shouldBe id2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val id1 = ApplicationId("app-123")
            val id2 = ApplicationId("app-456")

            // Assert
            id1 shouldNotBe id2
        }
    }

    @Nested
    inner class UsageTests {

        @Test
        fun `should work as map key`() {
            // Arrange
            val applicationId = ApplicationId("app-123")
            val map = mapOf(applicationId to "Application Data")

            // Act
            val result = map[ApplicationId("app-123")]

            // Assert
            result shouldBe "Application Data"
        }

        @Test
        fun `should work in set`() {
            // Arrange
            val id1 = ApplicationId("app-1")
            val id2 = ApplicationId("app-1")
            val id3 = ApplicationId("app-2")

            // Act
            val set = setOf(id1, id2, id3)

            // Assert
            set.size shouldBe 2
        }
    }
}
