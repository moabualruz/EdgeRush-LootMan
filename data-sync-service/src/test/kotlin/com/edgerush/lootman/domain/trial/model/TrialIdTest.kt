package com.edgerush.lootman.domain.trial.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

/**
 * Unit tests for TrialId value object.
 */
class TrialIdTest : UnitTest() {
    @Test
    fun `should create TrialId with valid value`() {
        // Arrange & Act
        val trialId = TrialId("trial-123")

        // Assert
        trialId.value shouldBe "trial-123"
    }

    @Test
    fun `should throw exception when value is blank`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            TrialId("")
        }.message shouldBe "Trial ID cannot be blank"
    }

    @Test
    fun `should throw exception when value is whitespace only`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            TrialId("   ")
        }.message shouldBe "Trial ID cannot be blank"
    }

    @Test
    fun `should generate unique IDs`() {
        // Arrange & Act
        val id1 = TrialId.generate()
        val id2 = TrialId.generate()

        // Assert
        id1.value.shouldNotBeBlank()
        id2.value.shouldNotBeBlank()
        id1 shouldNotBe id2
    }

    @Test
    fun `should have value equality`() {
        // Arrange
        val id1 = TrialId("same-value")
        val id2 = TrialId("same-value")

        // Assert
        id1 shouldBe id2
        id1.hashCode() shouldBe id2.hashCode()
    }
}
