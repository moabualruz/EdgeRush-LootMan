package com.edgerush.datasync.domain.flps.service

import com.edgerush.datasync.domain.flps.model.*
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FlpsCalculationServiceTest {

    private lateinit var service: FlpsCalculationService

    @BeforeEach
    fun setup() {
        service = FlpsCalculationService()
    }

    @Test
    fun `should calculate FLPS score correctly with all positive factors`() {
        // Given
        val raiderMerit = RaiderMeritScore.of(0.85)
        val itemPriority = ItemPriorityIndex.of(0.90)
        val recencyDecay = RecencyDecayFactor.of(1.0)

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        // Expected: (0.85 * 0.90) * 1.0 = 0.765
        result.value shouldBe 0.765
    }

    @Test
    fun `should calculate FLPS score with recency penalty`() {
        // Given
        val raiderMerit = RaiderMeritScore.of(0.90)
        val itemPriority = ItemPriorityIndex.of(0.80)
        val recencyDecay = RecencyDecayFactor.of(0.70)

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        // Expected: (0.90 * 0.80) * 0.70 = 0.504
        result.value shouldBe 0.504
    }

    @Test
    fun `should return zero FLPS when raider merit is zero`() {
        // Given
        val raiderMerit = RaiderMeritScore.zero()
        val itemPriority = ItemPriorityIndex.of(0.90)
        val recencyDecay = RecencyDecayFactor.noPenalty()

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        result.value shouldBe 0.0
    }

    @Test
    fun `should return zero FLPS when item priority is zero`() {
        // Given
        val raiderMerit = RaiderMeritScore.of(0.85)
        val itemPriority = ItemPriorityIndex.zero()
        val recencyDecay = RecencyDecayFactor.noPenalty()

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        result.value shouldBe 0.0
    }

    @Test
    fun `should return zero FLPS when recency decay is maximum penalty`() {
        // Given
        val raiderMerit = RaiderMeritScore.of(0.85)
        val itemPriority = ItemPriorityIndex.of(0.90)
        val recencyDecay = RecencyDecayFactor.maxPenalty()

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        result.value shouldBe 0.0
    }

    @Test
    fun `should calculate maximum FLPS with perfect scores`() {
        // Given
        val raiderMerit = RaiderMeritScore.perfect()
        val itemPriority = ItemPriorityIndex.max()
        val recencyDecay = RecencyDecayFactor.noPenalty()

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        result.value shouldBe 1.0
    }

    @Test
    fun `should always return score within valid range`() {
        // Given
        val raiderMerit = RaiderMeritScore.of(0.75)
        val itemPriority = ItemPriorityIndex.of(0.85)
        val recencyDecay = RecencyDecayFactor.of(0.95)

        // When
        val result = service.calculateFlpsScore(raiderMerit, itemPriority, recencyDecay)

        // Then
        result.value shouldBeGreaterThan 0.0
        result.value shouldBeLessThanOrEqual 1.0
    }

    @Test
    fun `should calculate attendance commitment score from percentage`() {
        // Given
        val attendancePercent = 95

        // When
        val score = service.calculateAttendanceCommitmentScore(attendancePercent)

        // Then
        score shouldBeGreaterThan 0.0
        score shouldBeLessThanOrEqual 1.0
    }

    @Test
    fun `should return high ACS for 100 percent attendance`() {
        // Given
        val attendancePercent = 100

        // When
        val score = service.calculateAttendanceCommitmentScore(attendancePercent)

        // Then
        score shouldBe 1.0
    }

    @Test
    fun `should return zero ACS for low attendance`() {
        // Given
        val attendancePercent = 50

        // When
        val score = service.calculateAttendanceCommitmentScore(attendancePercent)

        // Then
        score shouldBe 0.0
    }

    @Test
    fun `should calculate mechanical adherence score from performance metrics`() {
        // Given
        val deathsPerAttempt = 0.5
        val specAvgDpa = 0.6
        val avoidableDamagePct = 10.0
        val specAvgAdt = 12.0

        // When
        val score = service.calculateMechanicalAdherenceScore(
            deathsPerAttempt,
            specAvgDpa,
            avoidableDamagePct,
            specAvgAdt
        )

        // Then
        score shouldBeGreaterThan 0.0
        score shouldBeLessThanOrEqual 1.0
    }

    @Test
    fun `should return zero MAS for poor performance`() {
        // Given
        val deathsPerAttempt = 2.0
        val specAvgDpa = 0.5
        val avoidableDamagePct = 30.0
        val specAvgAdt = 10.0

        // When
        val score = service.calculateMechanicalAdherenceScore(
            deathsPerAttempt,
            specAvgDpa,
            avoidableDamagePct,
            specAvgAdt
        )

        // Then
        score shouldBe 0.0
    }

    @Test
    fun `should calculate external preparation score from activities`() {
        // Given
        val vaultSlots = 3
        val crestUsageRatio = 0.9
        val heroicKills = 6

        // When
        val score = service.calculateExternalPreparationScore(vaultSlots, crestUsageRatio, heroicKills)

        // Then
        score shouldBeGreaterThan 0.0
        score shouldBeLessThanOrEqual 1.0
    }

    @Test
    fun `should return low EPS for minimal preparation`() {
        // Given
        val vaultSlots = 0
        val crestUsageRatio = 0.0
        val heroicKills = 0

        // When
        val score = service.calculateExternalPreparationScore(vaultSlots, crestUsageRatio, heroicKills)

        // Then
        score shouldBe 0.0
    }
}
