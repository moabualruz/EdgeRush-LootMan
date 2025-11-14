package com.edgerush.lootman.domain.flps.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for FlpsCalculationService domain service.
 *
 * Tests the core FLPS calculation: FLPS = (RMS × IPI) × RDF
 */
class FlpsCalculationServiceTest : UnitTest() {
    private val service = FlpsCalculationService()

    @Test
    fun `should calculate FLPS from component scores`() {
        // Arrange
        val rms = RaiderMeritScore.of(0.8)
        val ipi = ItemPriorityIndex.of(0.9)
        val rdf = RecencyDecayFactor.of(1.0)

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        // FLPS = (0.8 × 0.9) × 1.0 = 0.72
        flps.value shouldBe 0.72
    }

    @Test
    fun `should calculate FLPS with recency decay`() {
        // Arrange
        val rms = RaiderMeritScore.of(0.8)
        val ipi = ItemPriorityIndex.of(0.9)
        val rdf = RecencyDecayFactor.of(0.85)

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        // FLPS = (0.8 × 0.9) × 0.85 = 0.612
        flps.value shouldBe 0.612
    }

    @Test
    fun `should return zero FLPS when RMS is zero`() {
        // Arrange
        val rms = RaiderMeritScore.zero()
        val ipi = ItemPriorityIndex.of(0.9)
        val rdf = RecencyDecayFactor.of(1.0)

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        flps.value shouldBe 0.0
    }

    @Test
    fun `should return zero FLPS when IPI is zero`() {
        // Arrange
        val rms = RaiderMeritScore.of(0.8)
        val ipi = ItemPriorityIndex.zero()
        val rdf = RecencyDecayFactor.of(1.0)

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        flps.value shouldBe 0.0
    }

    @Test
    fun `should return zero FLPS when RDF is zero`() {
        // Arrange
        val rms = RaiderMeritScore.of(0.8)
        val ipi = ItemPriorityIndex.of(0.9)
        val rdf = RecencyDecayFactor.zero()

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        flps.value shouldBe 0.0
    }

    @Test
    fun `should calculate max FLPS when all components are max`() {
        // Arrange
        val rms = RaiderMeritScore.max()
        val ipi = ItemPriorityIndex.max()
        val rdf = RecencyDecayFactor.noDecay()

        // Act
        val flps = service.calculateFlps(rms, ipi, rdf)

        // Assert
        // FLPS = (1.0 × 1.0) × 1.0 = 1.0
        flps.value shouldBe 1.0
    }

    @Test
    fun `should calculate FLPS from all individual components`() {
        // Arrange
        val acs = AttendanceCommitmentScore.of(0.9)
        val mas = MechanicalAdherenceScore.of(0.8)
        val eps = ExternalPreparationScore.of(0.7)
        val uv = UpgradeValue.of(0.8)
        val tb = TierBonus.of(1.1)
        val rm = RoleMultiplier.of(1.0)
        val rdf = RecencyDecayFactor.of(1.0)

        // Act
        val flps = service.calculateFlpsFromComponents(acs, mas, eps, uv, tb, rm, rdf)

        // Assert
        // RMS = (0.9 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2) = 0.82
        // IPI = (0.8 * 0.45) + (1.1 * 0.35) + (1.0 * 0.20) = 0.945
        // FLPS = (0.82 × 0.945) × 1.0 = 0.7749
        flps.value shouldBe 0.7749
    }
}
