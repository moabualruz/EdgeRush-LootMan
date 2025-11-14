package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for RecencyDecayFactor value object.
 *
 * RDF reduces FLPS for raiders who recently received loot.
 */
class RecencyDecayFactorTest : UnitTest() {
    @Test
    fun `should create valid RDF when value is between 0 and 1`() {
        // Arrange & Act
        val rdf = RecencyDecayFactor.of(0.85)

        // Assert
        rdf.value shouldBe 0.85
    }

    @Test
    fun `should create RDF with no decay`() {
        // Arrange & Act
        val rdf = RecencyDecayFactor.noDecay()

        // Assert
        rdf.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RecencyDecayFactor.of(-0.1)
            }
        exception.message shouldBe "Recency Decay Factor must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RecencyDecayFactor.of(1.5)
            }
        exception.message shouldBe "Recency Decay Factor must be between 0.0 and 1.0, got 1.5"
    }
}
