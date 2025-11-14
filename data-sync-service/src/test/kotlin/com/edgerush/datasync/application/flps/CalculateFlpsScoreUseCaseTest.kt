package com.edgerush.datasync.application.flps

import com.edgerush.datasync.domain.flps.model.*
import com.edgerush.datasync.domain.flps.repository.FlpsConfiguration
import com.edgerush.datasync.domain.flps.repository.FlpsModifierRepository
import com.edgerush.datasync.domain.flps.service.FlpsCalculationService
import com.edgerush.datasync.domain.flps.service.RaiderRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalculateFlpsScoreUseCaseTest {

    private lateinit var flpsCalculationService: FlpsCalculationService
    private lateinit var flpsModifierRepository: FlpsModifierRepository
    private lateinit var useCase: CalculateFlpsScoreUseCase

    @BeforeEach
    fun setup() {
        flpsCalculationService = FlpsCalculationService()
        flpsModifierRepository = mockk()
        useCase = CalculateFlpsScoreUseCase(flpsCalculationService, flpsModifierRepository)
    }

    @Test
    fun `should calculate FLPS score with valid inputs`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 2.0,
            specAvgAdt = 3.0,
            vaultSlots = 3,
            crestUsageRatio = 0.8,
            heroicKills = 6,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = RaiderRole.DPS,
            recentLootCount = 0
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val flpsResult = result.getOrThrow()
        
        assertNotNull(flpsResult.flpsScore)
        assertTrue(flpsResult.flpsScore.value in 0.0..1.0)
        assertTrue(flpsResult.raiderMerit.value in 0.0..1.0)
        assertTrue(flpsResult.itemPriority.value in 0.0..1.0)
        assertTrue(flpsResult.recencyDecay.value in 0.0..1.0)
        
        verify(exactly = 1) { flpsModifierRepository.findByGuildId("test-guild") }
    }

    @Test
    fun `should calculate correct component scores`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 100,
            deathsPerAttempt = 0.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = 0.0,
            specAvgAdt = 3.0,
            vaultSlots = 9,
            crestUsageRatio = 1.0,
            heroicKills = 8,
            simulatedGain = 1000.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 0,
            role = RaiderRole.DPS,
            recentLootCount = 0
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val flpsResult = result.getOrThrow()
        
        // Perfect attendance should give 1.0
        assertEquals(1.0, flpsResult.attendanceScore, 0.01)
        
        // Perfect mechanical should give 1.0
        assertEquals(1.0, flpsResult.mechanicalScore, 0.01)
        
        // Perfect preparation should give 1.0
        assertEquals(1.0, flpsResult.preparationScore, 0.01)
        
        // No recent loot should give 1.0
        assertEquals(1.0, flpsResult.recencyDecay.value, 0.01)
    }

    @Test
    fun `should apply recency decay for recent loot`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 100,
            deathsPerAttempt = 0.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = 0.0,
            specAvgAdt = 3.0,
            vaultSlots = 9,
            crestUsageRatio = 1.0,
            heroicKills = 8,
            simulatedGain = 1000.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 0,
            role = RaiderRole.DPS,
            recentLootCount = 2
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val flpsResult = result.getOrThrow()
        
        // Recent loot should reduce RDF below 1.0
        assertTrue(flpsResult.recencyDecay.value < 1.0)
        assertTrue(flpsResult.recencyDecay.value >= 0.0)
    }

    @Test
    fun `should handle low attendance correctly`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 50,
            deathsPerAttempt = 0.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = 0.0,
            specAvgAdt = 3.0,
            vaultSlots = 3,
            crestUsageRatio = 0.5,
            heroicKills = 3,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = RaiderRole.DPS,
            recentLootCount = 0
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val flpsResult = result.getOrThrow()
        
        // Low attendance should give 0.0
        assertEquals(0.0, flpsResult.attendanceScore, 0.01)
    }

    @Test
    fun `should apply role multipliers correctly`() {
        // Given
        val dpsCommand = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 100,
            deathsPerAttempt = 0.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = 0.0,
            specAvgAdt = 3.0,
            vaultSlots = 3,
            crestUsageRatio = 0.8,
            heroicKills = 6,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = RaiderRole.DPS,
            recentLootCount = 0
        )

        val tankCommand = dpsCommand.copy(role = RaiderRole.TANK)
        val healerCommand = dpsCommand.copy(role = RaiderRole.HEALER)

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val dpsResult = useCase.execute(dpsCommand).getOrThrow()
        val tankResult = useCase.execute(tankCommand).getOrThrow()
        val healerResult = useCase.execute(healerCommand).getOrThrow()

        // Then
        // DPS should have highest role multiplier (1.0)
        assertEquals(1.0, dpsResult.roleMultiplier, 0.01)
        
        // Tank should have lower multiplier (0.8)
        assertEquals(0.8, tankResult.roleMultiplier, 0.01)
        
        // Healer should have lowest multiplier (0.7)
        assertEquals(0.7, healerResult.roleMultiplier, 0.01)
        
        // DPS should have highest IPI
        assertTrue(dpsResult.itemPriority.value >= tankResult.itemPriority.value)
        assertTrue(tankResult.itemPriority.value >= healerResult.itemPriority.value)
    }

    @Test
    fun `should handle negative inputs gracefully`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = -10,
            deathsPerAttempt = -1.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = -5.0,
            specAvgAdt = 3.0,
            vaultSlots = -2,
            crestUsageRatio = -0.5,
            heroicKills = -3,
            simulatedGain = -100.0,
            specBaseline = 10000.0,
            tierPiecesOwned = -1,
            role = RaiderRole.DPS,
            recentLootCount = -1
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val flpsResult = result.getOrThrow()
        
        // Should handle negatives gracefully and produce valid scores
        assertTrue(flpsResult.flpsScore.value in 0.0..1.0)
        assertTrue(flpsResult.raiderMerit.value in 0.0..1.0)
        assertTrue(flpsResult.itemPriority.value in 0.0..1.0)
        assertTrue(flpsResult.recencyDecay.value in 0.0..1.0)
    }

    @Test
    fun `should return failure when repository throws exception`() {
        // Given
        val command = CalculateFlpsScoreCommand(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 2.0,
            specAvgAdt = 3.0,
            vaultSlots = 3,
            crestUsageRatio = 0.8,
            heroicKills = 6,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = RaiderRole.DPS,
            recentLootCount = 0
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } throws RuntimeException("Database error")

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}
