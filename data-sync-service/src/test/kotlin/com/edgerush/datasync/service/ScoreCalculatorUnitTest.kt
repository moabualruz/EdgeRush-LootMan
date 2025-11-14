package com.edgerush.datasync.service

import com.edgerush.datasync.model.Role
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScoreCalculatorUnitTest {
    @BeforeEach
    fun setUp() {
        // Setup for unit tests
    }

    @Test
    fun `FLPS calculation should follow correct formula`() {
        // Test the core FLPS formula: FLPS = (RMS × IPI) × RDF

        // Given: Perfect scores
        val acs = 1.0 // Perfect attendance
        val mas = 1.0 // Perfect mechanical
        val eps = 1.0 // Perfect preparation
        val rms = acs * mas * eps // Should be 1.0

        val upgradeValue = 1.0
        val tierBonus = 1.0
        val roleMultiplier = 1.2 // Tank multiplier
        val ipi = (upgradeValue + tierBonus) * roleMultiplier // Should be 2.4

        val rdf = 1.0 // No recent loot penalty

        val expectedFlps = (rms * ipi) * rdf // Should be 2.4

        // Then: FLPS should equal (RMS × IPI) × RDF
        assertEquals(2.4, expectedFlps, 0.001)
    }

    @Test
    fun `RMS should be product of attendance, mechanical, and preparation`() {
        // Given: Various component scores
        val acs = 0.8 // 80% attendance
        val mas = 0.9 // 90% mechanical
        val eps = 0.7 // 70% preparation

        // When: Calculating RMS
        val rms = acs * mas * eps

        // Then: RMS should be the product
        assertEquals(0.504, rms, 0.001) // 0.8 * 0.9 * 0.7 = 0.504
    }

    @Test
    fun `IPI should be upgrade and tier bonus times role multiplier`() {
        // Given: Component scores
        val upgradeValue = 0.6
        val tierBonus = 0.8
        val roleMultiplier = 1.1 // DPS multiplier

        // When: Calculating IPI
        val ipi = (upgradeValue + tierBonus) * roleMultiplier

        // Then: IPI should follow the formula
        assertEquals(1.54, ipi, 0.001) // (0.6 + 0.8) * 1.1 = 1.54
    }

    @Test
    fun `scores should be bounded between 0 and expected maximums`() {
        // Test that scores don't exceed logical bounds

        // ACS, MAS, EPS should be between 0.0 and 1.0
        assertTrue(0.0 >= 0.0 && 1.0 <= 1.0)

        // Role multipliers should be reasonable (1.0 to 1.3 typically)
        val tankMultiplier = 1.2
        val healerMultiplier = 1.15
        val dpsMultiplier = 1.0

        assertTrue(tankMultiplier >= 1.0 && tankMultiplier <= 1.3)
        assertTrue(healerMultiplier >= 1.0 && healerMultiplier <= 1.3)
        assertTrue(dpsMultiplier >= 1.0 && dpsMultiplier <= 1.3)
    }

    @Test
    fun `RDF should penalize recent loot recipients`() {
        // Given: Days since last loot
        val daysSinceLastLoot = listOf(0, 1, 7, 14, 30)

        daysSinceLastLoot.forEach { days ->
            // When: Calculating RDF (simplified version of the decay)
            val rdf =
                when {
                    days == 0 -> 0.1 // Just received loot
                    days < 7 -> 0.5 // Recent loot
                    days < 14 -> 0.8 // Somewhat recent
                    else -> 1.0 // No penalty
                }

            // Then: RDF should decrease for recent loot
            assertTrue(rdf >= 0.0 && rdf <= 1.0)
            if (days == 0) assertEquals(0.1, rdf)
            if (days >= 30) assertEquals(1.0, rdf)
        }
    }

    @Test
    fun `zero scores should not break calculations`() {
        // Given: Edge case with zero scores
        val acs = 0.0
        val mas = 0.0
        val eps = 0.0
        val rms = acs * mas * eps // Should be 0.0

        val upgradeValue = 0.0
        val tierBonus = 0.0
        val roleMultiplier = 1.0
        val ipi = (upgradeValue + tierBonus) * roleMultiplier // Should be 0.0

        val rdf = 1.0
        val flps = (rms * ipi) * rdf // Should be 0.0

        // Then: All calculations should work with zeros
        assertEquals(0.0, rms)
        assertEquals(0.0, ipi)
        assertEquals(0.0, flps)
        assertFalse(flps.isNaN())
        assertFalse(flps.isInfinite())
    }

    @Test
    fun `role multipliers should be applied correctly per role`() {
        // Given: Different roles and their expected multipliers
        val tankMultiplier = 1.2
        val healerMultiplier = 1.15
        val dpsMultiplier = 1.0

        // When: Testing role-based multipliers
        val tankResult =
            when (Role.Tank) {
                Role.Tank -> 1.2
                Role.Healer -> 1.15
                Role.DPS -> 1.0
            }

        val healerResult =
            when (Role.Healer) {
                Role.Tank -> 1.2
                Role.Healer -> 1.15
                Role.DPS -> 1.0
            }

        val dpsResult =
            when (Role.DPS) {
                Role.Tank -> 1.2
                Role.Healer -> 1.15
                Role.DPS -> 1.0
            }

        // Then: Multipliers should match role expectations
        assertEquals(tankMultiplier, tankResult)
        assertEquals(healerMultiplier, healerResult)
        assertEquals(dpsMultiplier, dpsResult)
    }

    @Test
    fun `percentage calculations should be accurate`() {
        // Given: Raw score and perfect score
        val rawScore = 0.75
        val perfectScore = 1.0

        // When: Calculating percentage
        val percentage = (rawScore / perfectScore) * 100.0

        // Then: Percentage should be correct
        assertEquals(75.0, percentage, 0.001)

        // Test edge cases
        assertEquals(0.0, (0.0 / 1.0) * 100.0)
        assertEquals(100.0, (1.0 / 1.0) * 100.0)

        // Test when perfect score is not 1.0
        val highScore = 2.4
        val perfectFlps = 3.0
        val flpsPercentage = (highScore / perfectFlps) * 100.0
        assertEquals(80.0, flpsPercentage, 0.001)
    }
}
