package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.flps.CalculateFlpsScoreCommand
import com.edgerush.lootman.application.flps.CalculateFlpsScoreUseCase
import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.application.flps.FlpsDataAssemblerService
import com.edgerush.lootman.application.flps.FlpsReport
import com.edgerush.lootman.application.flps.GetFlpsReportQuery
import com.edgerush.lootman.application.flps.GetFlpsReportUseCase
import com.edgerush.lootman.application.flps.RaiderFlpsData
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for FlpsController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases and services as dependencies.
 */
class FlpsControllerTest : UnitTest() {
    private lateinit var calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase
    private lateinit var getFlpsReportUseCase: GetFlpsReportUseCase
    private lateinit var flpsDataAssembler: FlpsDataAssemblerService
    private lateinit var componentCalculator: FlpsComponentCalculator
    private lateinit var controller: FlpsController

    @BeforeEach
    fun setup() {
        calculateFlpsScoreUseCase = mockk()
        getFlpsReportUseCase = mockk()
        flpsDataAssembler = mockk()
        componentCalculator = mockk()
        controller = FlpsController(
            calculateFlpsScoreUseCase,
            getFlpsReportUseCase,
            flpsDataAssembler,
            componentCalculator,
        )
    }

    @Test
    fun `getFlpsReportV1 should return report response from use case`() {
        // Given
        val guildId = "test-guild"
        val flpsReport = FlpsReport(
            guildId = GuildId(guildId),
            calculations = emptyList(),
        )

        every { getFlpsReportUseCase.execute(any()) } returns Result.success(flpsReport)

        // When
        val response = controller.getFlpsReportV1(guildId)

        // Then
        response.guildId shouldBe guildId
        response.calculations shouldHaveSize 0

        verify(exactly = 1) { getFlpsReportUseCase.execute(any()) }
    }

    @Test
    fun `getFlpsReportV1 should throw exception when use case returns failure`() {
        // Given
        val guildId = "failing-guild"
        val exception = RuntimeException("Failed to generate FLPS report")

        every { getFlpsReportUseCase.execute(any()) } returns Result.failure(exception)

        // When / Then
        val thrownException = assertThrows<RuntimeException> {
            controller.getFlpsReportV1(guildId)
        }

        thrownException.message shouldBe "Failed to generate FLPS report"
        verify(exactly = 1) { getFlpsReportUseCase.execute(any()) }
    }

    @Test
    fun `getFlpsReportV1 should include calculations in response`() {
        // Given
        val guildId = "test-guild"
        val calculation = createFlpsCalculationResult(guildId, 1L, 0.85)
        val flpsReport = FlpsReport(
            guildId = GuildId(guildId),
            calculations = listOf(calculation),
        )

        every { getFlpsReportUseCase.execute(any()) } returns Result.success(flpsReport)

        // When
        val response = controller.getFlpsReportV1(guildId)

        // Then
        response.guildId shouldBe guildId
        response.calculations shouldHaveSize 1
        response.calculations[0].raiderId shouldBe "1"
        response.calculations[0].flpsScore shouldBe 0.85

        verify(exactly = 1) { getFlpsReportUseCase.execute(any()) }
    }

    @Test
    fun `getBenchmarks should return default benchmark values`() {
        // Given
        val guildId = "test-guild"

        // When
        val response = controller.getBenchmarks(guildId)

        // Then
        response.theoretical shouldBe 1.0
        response.topPerformer shouldBe 0.95
    }

    @Test
    fun `getStatus should return system status with features and endpoints`() {
        // When
        val response = controller.getStatus()

        // Then
        response.message shouldBe "FLPS calculation system using domain-driven architecture"
        response.features.size shouldBe 6
        response.endpoints.size shouldBe 5
        response.endpoints["Guild Report"] shouldBe "/api/flps/{guildId}"
    }

    @Test
    fun `getStatusV1 should return v1 status response with correct endpoints`() {
        // When
        val response = controller.getStatusV1()

        // Then
        response.message shouldBe "FLPS calculation system using domain-driven architecture"
        response.features.size shouldBe 6
        response.endpoints.size shouldBe 2
        response.endpoints["Guild Report"] shouldBe "/api/v1/flps/guilds/{guildId}/report"
        response.endpoints["System Status"] shouldBe "/api/v1/flps/status"
    }

    @Test
    fun `getFlpsReport should return comprehensive report for guild raiders`() {
        // Given
        val guildId = "test-guild"
        val raider = mockk<Raider>()
        every { raider.id } returns RaiderId(123L)
        every { raider.characterName } returns "TestRaider"
        every { raider.role } returns Role.DPS

        val raiderData = RaiderFlpsData(
            raider = raider,
            attendance = emptyList(),
            lootHistory = emptyList(),
            wishlist = null,
            gear = null,
            activeBans = emptyList(),
        )

        val calculationResult = createFlpsCalculationResult(guildId, 123L, 0.75)

        every { flpsDataAssembler.assembleFlpsData(GuildId(guildId)) } returns listOf(raiderData)
        every { componentCalculator.calculateACS(any()) } returns AttendanceCommitmentScore.of(0.9)
        every { componentCalculator.calculateMAS() } returns MechanicalAdherenceScore.of(0.8)
        every { componentCalculator.calculateEPS(any()) } returns ExternalPreparationScore.of(0.7)
        every { componentCalculator.calculateUV(any(), any()) } returns UpgradeValue.of(0.6)
        every { componentCalculator.calculateTierBonus(any()) } returns TierBonus.of(1.1)
        every { componentCalculator.calculateRoleMultiplier(any()) } returns RoleMultiplier.of(1.0)
        every { componentCalculator.calculateRDF(any(), any()) } returns RecencyDecayFactor.of(1.0)
        every { calculateFlpsScoreUseCase.execute(any()) } returns Result.success(calculationResult)

        // When
        val response = controller.getFlpsReport(guildId)

        // Then
        response shouldHaveSize 1
        response[0].raiderId shouldBe "123"
        response[0].raiderName shouldBe "TestRaider"
        response[0].flpsScore shouldBe 0.75
        response[0].eligible shouldBe true

        verify(exactly = 1) { flpsDataAssembler.assembleFlpsData(GuildId(guildId)) }
        verify(exactly = 1) { calculateFlpsScoreUseCase.execute(any()) }
    }

    @Test
    fun `getFlpsReport should sort results by FLPS score descending`() {
        // Given
        val guildId = "test-guild"

        val raider1 = mockk<Raider>()
        every { raider1.id } returns RaiderId(1L)
        every { raider1.characterName } returns "LowScoreRaider"
        every { raider1.role } returns Role.DPS

        val raider2 = mockk<Raider>()
        every { raider2.id } returns RaiderId(2L)
        every { raider2.characterName } returns "HighScoreRaider"
        every { raider2.role } returns Role.TANK

        val raiderData1 = RaiderFlpsData(
            raider = raider1,
            attendance = emptyList(),
            lootHistory = emptyList(),
            wishlist = null,
            gear = null,
            activeBans = emptyList(),
        )

        val raiderData2 = RaiderFlpsData(
            raider = raider2,
            attendance = emptyList(),
            lootHistory = emptyList(),
            wishlist = null,
            gear = null,
            activeBans = emptyList(),
        )

        val lowResult = createFlpsCalculationResult(guildId, 1L, 0.50)
        val highResult = createFlpsCalculationResult(guildId, 2L, 0.90)

        every { flpsDataAssembler.assembleFlpsData(GuildId(guildId)) } returns listOf(raiderData1, raiderData2)
        every { componentCalculator.calculateACS(any()) } returns AttendanceCommitmentScore.of(0.9)
        every { componentCalculator.calculateMAS() } returns MechanicalAdherenceScore.of(0.8)
        every { componentCalculator.calculateEPS(any()) } returns ExternalPreparationScore.of(0.7)
        every { componentCalculator.calculateUV(any(), any()) } returns UpgradeValue.of(0.6)
        every { componentCalculator.calculateTierBonus(any()) } returns TierBonus.of(1.1)
        every { componentCalculator.calculateRoleMultiplier(any()) } returns RoleMultiplier.of(1.0)
        every { componentCalculator.calculateRDF(any(), any()) } returns RecencyDecayFactor.of(1.0)

        every { calculateFlpsScoreUseCase.execute(match { it.raiderId == RaiderId(1L) }) } returns Result.success(lowResult)
        every { calculateFlpsScoreUseCase.execute(match { it.raiderId == RaiderId(2L) }) } returns Result.success(highResult)

        // When
        val response = controller.getFlpsReport(guildId)

        // Then
        response shouldHaveSize 2
        response[0].raiderName shouldBe "HighScoreRaider"
        response[0].flpsScore shouldBe 0.90
        response[1].raiderName shouldBe "LowScoreRaider"
        response[1].flpsScore shouldBe 0.50
    }

    @Test
    fun `getFlpsReport should return empty list when no raiders in guild`() {
        // Given
        val guildId = "empty-guild"

        every { flpsDataAssembler.assembleFlpsData(GuildId(guildId)) } returns emptyList()

        // When
        val response = controller.getFlpsReport(guildId)

        // Then
        response shouldHaveSize 0

        verify(exactly = 1) { flpsDataAssembler.assembleFlpsData(GuildId(guildId)) }
        verify(exactly = 0) { calculateFlpsScoreUseCase.execute(any()) }
    }

    private fun createFlpsCalculationResult(
        guildId: String,
        raiderId: Long,
        flpsScore: Double,
    ): FlpsCalculationResult {
        return FlpsCalculationResult(
            guildId = GuildId(guildId),
            raiderId = RaiderId(raiderId),
            itemId = ItemId(12345L),
            acs = AttendanceCommitmentScore.of(0.9),
            mas = MechanicalAdherenceScore.of(0.8),
            eps = ExternalPreparationScore.of(0.7),
            rms = RaiderMeritScore.of(0.82),
            uv = UpgradeValue.of(0.8),
            tb = TierBonus.of(1.1),
            rm = RoleMultiplier.of(1.0),
            ipi = ItemPriorityIndex.of(0.945),
            rdf = RecencyDecayFactor.of(1.0),
            flps = FlpsScore.of(flpsScore),
            eligible = true,
        )
    }
}
