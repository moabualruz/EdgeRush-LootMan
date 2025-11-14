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

class GetFlpsReportUseCaseTest {

    private lateinit var flpsCalculationService: FlpsCalculationService
    private lateinit var flpsModifierRepository: FlpsModifierRepository
    private lateinit var useCase: GetFlpsReportUseCase

    @BeforeEach
    fun setup() {
        flpsCalculationService = FlpsCalculationService()
        flpsModifierRepository = mockk()
        useCase = GetFlpsReportUseCase(flpsCalculationService, flpsModifierRepository)
    }

    @Test
    fun `should generate report for single raider`() {
        // Given
        val raider = RaiderFlpsData(
            raiderId = "raider-1",
            raiderName = "TestRaider",
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

        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        
        assertEquals(1, report.raiderReports.size)
        
        val raiderReport = report.raiderReports[0]
        assertEquals("raider-1", raiderReport.raiderId)
        assertEquals("TestRaider", raiderReport.raiderName)
        assertNotNull(raiderReport.flpsScore)
        assertTrue(raiderReport.flpsScore.value in 0.0..1.0)
        
        verify(exactly = 1) { flpsModifierRepository.findByGuildId("test-guild") }
    }

    @Test
    fun `should generate report for multiple raiders`() {
        // Given
        val raider1 = RaiderFlpsData(
            raiderId = "raider-1",
            raiderName = "HighScorer",
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

        val raider2 = RaiderFlpsData(
            raiderId = "raider-2",
            raiderName = "LowScorer",
            attendancePercent = 50,
            deathsPerAttempt = 2.0,
            specAvgDpa = 1.0,
            avoidableDamagePct = 10.0,
            specAvgAdt = 3.0,
            vaultSlots = 0,
            crestUsageRatio = 0.0,
            heroicKills = 0,
            simulatedGain = 100.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 4,
            role = RaiderRole.HEALER,
            recentLootCount = 3
        )

        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider1, raider2)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        
        assertEquals(2, report.raiderReports.size)
        
        val highScorer = report.raiderReports.find { it.raiderId == "raider-1" }!!
        val lowScorer = report.raiderReports.find { it.raiderId == "raider-2" }!!
        
        // High scorer should have better FLPS
        assertTrue(highScorer.flpsScore > lowScorer.flpsScore)
    }

    @Test
    fun `should sort raiders by FLPS score descending`() {
        // Given
        val raiders = listOf(
            createRaiderData("raider-1", "Medium", attendancePercent = 85),
            createRaiderData("raider-2", "High", attendancePercent = 100),
            createRaiderData("raider-3", "Low", attendancePercent = 50)
        )

        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = raiders
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        
        assertEquals(3, report.raiderReports.size)
        
        // Should be sorted by FLPS descending
        assertTrue(report.raiderReports[0].flpsScore >= report.raiderReports[1].flpsScore)
        assertTrue(report.raiderReports[1].flpsScore >= report.raiderReports[2].flpsScore)
        
        // High attendance should be first
        assertEquals("High", report.raiderReports[0].raiderName)
    }

    @Test
    fun `should include component breakdowns in report`() {
        // Given
        val raider = createRaiderData("raider-1", "TestRaider", attendancePercent = 95)
        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        val raiderReport = report.raiderReports[0]
        
        // Should have all component scores
        assertNotNull(raiderReport.raiderMerit)
        assertNotNull(raiderReport.itemPriority)
        assertNotNull(raiderReport.recencyDecay)
        assertNotNull(raiderReport.attendanceScore)
        assertNotNull(raiderReport.mechanicalScore)
        assertNotNull(raiderReport.preparationScore)
        assertNotNull(raiderReport.upgradeValue)
        assertNotNull(raiderReport.tierBonus)
        assertNotNull(raiderReport.roleMultiplier)
    }

    @Test
    fun `should handle empty raider list`() {
        // Given
        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = emptyList()
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        
        assertEquals(0, report.raiderReports.size)
        assertEquals("test-guild", report.guildId)
    }

    @Test
    fun `should include guild configuration in report`() {
        // Given
        val raider = createRaiderData("raider-1", "TestRaider")
        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider)
        )

        val customConfig = FlpsConfiguration.default().copy(
            rmsWeights = RmsWeights(0.5, 0.3, 0.2)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns customConfig

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        
        assertEquals(customConfig, report.configuration)
        assertEquals(0.5, report.configuration.rmsWeights.attendance)
    }

    @Test
    fun `should return failure when repository throws exception`() {
        // Given
        val raider = createRaiderData("raider-1", "TestRaider")
        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } throws RuntimeException("Database error")

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should calculate percentages relative to perfect scores`() {
        // Given
        val raider = RaiderFlpsData(
            raiderId = "raider-1",
            raiderName = "TestRaider",
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

        val query = GetFlpsReportQuery(
            guildId = "test-guild",
            raiders = listOf(raider)
        )

        every { flpsModifierRepository.findByGuildId("test-guild") } returns FlpsConfiguration.default()

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        val raiderReport = report.raiderReports[0]
        
        // Perfect scores should give 100% percentages
        assertEquals(100.0, raiderReport.attendancePercentage, 0.1)
        assertEquals(100.0, raiderReport.mechanicalPercentage, 0.1)
        assertEquals(100.0, raiderReport.preparationPercentage, 0.1)
        assertEquals(100.0, raiderReport.recencyDecayPercentage, 0.1)
    }

    private fun createRaiderData(
        raiderId: String,
        raiderName: String,
        attendancePercent: Int = 95
    ): RaiderFlpsData {
        return RaiderFlpsData(
            raiderId = raiderId,
            raiderName = raiderName,
            attendancePercent = attendancePercent,
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
    }
}
