package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.application.flps.FlpsDataAssemblerService
import com.edgerush.lootman.application.flps.RaiderFlpsData
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.model.RaiderPreparationData
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.repository.FlpsThresholds
import com.edgerush.lootman.domain.flps.repository.IpiWeights
import com.edgerush.lootman.domain.flps.repository.RmsWeights
import com.edgerush.lootman.domain.flps.repository.RoleMultipliers
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.datasync.test.fixtures.RaiderFixtures
import com.edgerush.lootman.domain.shared.model.Raider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for FlpsConfigPreviewService.
 *
 * Tests the configuration preview functionality following TDD principles.
 */
class FlpsConfigPreviewServiceTest : UnitTest() {
    private val modifierRepository = mockk<FlpsModifierRepository>()
    private val flpsDataAssembler = mockk<FlpsDataAssemblerService>()
    private val componentCalculator = mockk<FlpsComponentCalculator>()
    private val flpsCalculationService = mockk<FlpsCalculationService>()

    private lateinit var service: FlpsConfigPreviewService

    private val guildId = "guild-123"
    private val guildIdObj = GuildId(guildId)

    @BeforeEach
    fun setUp() {
        service =
            FlpsConfigPreviewService(
                modifierRepository = modifierRepository,
                flpsDataAssembler = flpsDataAssembler,
                componentCalculator = componentCalculator,
                flpsCalculationService = flpsCalculationService,
            )
    }

    @Nested
    inner class GetCurrentConfig {
        @Test
        fun `should return current configuration for guild`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers

            // Act
            val result = service.getCurrentConfig(guildId)

            // Assert
            result.rmsWeights.attendance shouldBe 0.4
            result.rmsWeights.mechanical shouldBe 0.4
            result.rmsWeights.preparation shouldBe 0.2
            result.ipiWeights.upgradeValue shouldBe 0.45
            result.thresholds.eligibilityAttendance shouldBe 0.8
        }
    }

    @Nested
    inner class PreviewConfigChanges {
        @Test
        fun `should return preview with current and proposed configs`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers
            every { flpsDataAssembler.assembleFlpsData(guildIdObj) } returns emptyList()

            val request =
                ConfigPreviewRequest(
                    rmsWeights = RmsWeightsRequest(attendance = 0.5),
                )

            // Act
            val result = service.previewConfigChanges(guildId, request)

            // Assert
            result.guildId shouldBe guildId
            result.currentConfig.rmsWeights.attendance shouldBe 0.4
            result.proposedConfig.rmsWeights.attendance shouldBe 0.5
        }

        @Test
        fun `should preserve unchanged values in proposed config`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers
            every { flpsDataAssembler.assembleFlpsData(guildIdObj) } returns emptyList()

            val request =
                ConfigPreviewRequest(
                    rmsWeights = RmsWeightsRequest(attendance = 0.5),
                    // Other values are null, should preserve current values
                )

            // Act
            val result = service.previewConfigChanges(guildId, request)

            // Assert
            result.proposedConfig.rmsWeights.mechanical shouldBe 0.4 // unchanged
            result.proposedConfig.ipiWeights.upgradeValue shouldBe 0.45 // unchanged
            result.proposedConfig.thresholds.eligibilityAttendance shouldBe 0.8 // unchanged
        }

        @Test
        fun `should calculate raider impacts when config changes`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val raiderData = createTestRaiderData()

            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers
            every { flpsDataAssembler.assembleFlpsData(guildIdObj) } returns listOf(raiderData)
            setupComponentCalculatorMocks()
            setupFlpsCalculationMocks()

            val request =
                ConfigPreviewRequest(
                    rmsWeights = RmsWeightsRequest(attendance = 0.6), // Increase attendance weight
                )

            // Act
            val result = service.previewConfigChanges(guildId, request)

            // Assert
            result.raiderImpacts.size shouldBe 1
            result.raiderImpacts[0].raiderId shouldBe 42L
        }

        @Test
        fun `should calculate impact summary`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val raiderData = createTestRaiderData()

            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers
            every { flpsDataAssembler.assembleFlpsData(guildIdObj) } returns listOf(raiderData)
            setupComponentCalculatorMocks()
            setupFlpsCalculationMocks()

            val request = ConfigPreviewRequest()

            // Act
            val result = service.previewConfigChanges(guildId, request)

            // Assert
            result.impactSummary shouldNotBe null
            result.impactSummary.eligibilityChanges shouldNotBe null
        }

        @Test
        fun `should detect eligibility changes when threshold changes`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val raiderData = createTestRaiderData()

            every { modifierRepository.findByGuildId(guildIdObj) } returns modifiers
            every { flpsDataAssembler.assembleFlpsData(guildIdObj) } returns listOf(raiderData)
            setupComponentCalculatorMocks()
            setupFlpsCalculationMocks()

            // Raise eligibility threshold above raider's attendance
            val request =
                ConfigPreviewRequest(
                    thresholds = ThresholdsRequest(eligibilityAttendance = 0.95),
                )

            // Act
            val result = service.previewConfigChanges(guildId, request)

            // Assert - raider with 0.85 attendance should lose eligibility
            // when threshold raised from 0.8 to 0.95
            result.impactSummary.eligibilityChanges.lost shouldBe 1
        }
    }

    @Nested
    inner class MergeWithExtension {
        @Test
        fun `should merge partial request with current modifiers`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val request =
                ConfigPreviewRequest(
                    rmsWeights = RmsWeightsRequest(attendance = 0.6),
                )

            // Act
            val merged = modifiers.mergeWith(request)

            // Assert
            merged.rmsWeights.attendance shouldBe 0.6
            merged.rmsWeights.mechanical shouldBe 0.4 // unchanged
            merged.rmsWeights.preparation shouldBe 0.2 // unchanged
        }

        @Test
        fun `should merge all fields when provided`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val request =
                ConfigPreviewRequest(
                    rmsWeights =
                        RmsWeightsRequest(
                            attendance = 0.5,
                            mechanical = 0.3,
                            preparation = 0.2,
                        ),
                    ipiWeights =
                        IpiWeightsRequest(
                            upgradeValue = 0.5,
                            tierBonus = 0.3,
                            roleMultiplier = 0.2,
                        ),
                    roleMultipliers =
                        RoleMultipliersRequest(
                            dps = 1.0,
                            tank = 1.3,
                            healer = 1.2,
                        ),
                    thresholds =
                        ThresholdsRequest(
                            eligibilityAttendance = 0.9,
                            eligibilityActivity = 0.1,
                        ),
                )

            // Act
            val merged = modifiers.mergeWith(request)

            // Assert
            merged.rmsWeights.attendance shouldBe 0.5
            merged.rmsWeights.mechanical shouldBe 0.3
            merged.ipiWeights.upgradeValue shouldBe 0.5
            merged.roleMultipliers.tank shouldBe 1.3
            merged.thresholds.eligibilityAttendance shouldBe 0.9
        }

        @Test
        fun `should preserve all values when request is empty`() {
            // Arrange
            val modifiers = createDefaultModifiers()
            val request = ConfigPreviewRequest()

            // Act
            val merged = modifiers.mergeWith(request)

            // Assert
            merged shouldBe modifiers
        }
    }

    // Helper methods
    private fun createDefaultModifiers(): FlpsModifiers =
        FlpsModifiers(
            guildId = guildIdObj,
            rmsWeights =
                RmsWeights(
                    attendance = 0.4,
                    mechanical = 0.4,
                    preparation = 0.2,
                ),
            ipiWeights =
                IpiWeights(
                    upgradeValue = 0.45,
                    tierBonus = 0.35,
                    roleMultiplier = 0.20,
                ),
            roleMultipliers =
                RoleMultipliers(
                    dps = 1.0,
                    tank = 0.8,
                    healer = 0.7,
                ),
            thresholds =
                FlpsThresholds(
                    eligibilityAttendance = 0.8,
                    eligibilityActivity = 0.0,
                ),
        )

    private fun createTestRaiderData(): RaiderFlpsData {
        val raider = RaiderFixtures.createRaider(
            id = RaiderId(42L),
            guildId = guildIdObj,
            name = "TestRaider",
            realm = "TestRealm",
            characterClass = CharacterClass.WARRIOR,
            role = Role.DPS,
            rank = "Raider",
            status = RaiderStatus.ACTIVE,
            joinDate = null,
            wowauditId = null,
        )
        return RaiderFlpsData(
            raider = raider,
            attendance = emptyList<AttendanceRecord>(),
            gear = null,
            wishlist = null,
            lootHistory = emptyList<LootAward>(),
            activeBans = emptyList<LootBan>(),
            performanceData = RaiderPerformanceData.empty(RaiderId(42L), "TestRaider", "TestRealm"),
            preparation = RaiderPreparationData.empty(RaiderId(42L)),
        )
    }

    private fun setupComponentCalculatorMocks() {
        every { componentCalculator.calculateACS(any()) } returns AttendanceCommitmentScore.of(0.85)
        every { componentCalculator.calculateMAS(any()) } returns MechanicalAdherenceScore.of(0.75)
        every { componentCalculator.calculateEPS(any(), any()) } returns ExternalPreparationScore.of(0.70)
        every { componentCalculator.calculateUV(any(), any()) } returns UpgradeValue.of(0.60)
        every { componentCalculator.calculateTierBonus(any()) } returns TierBonus.of(0.50)
        every { componentCalculator.calculateRoleMultiplier(any()) } returns RoleMultiplier.of(1.0)
        every { componentCalculator.calculateRDF(any(), any()) } returns RecencyDecayFactor.of(0.90)
    }

    private fun setupFlpsCalculationMocks() {
        every {
            flpsCalculationService.calculateFlps(any<RaiderMeritScore>(), any<ItemPriorityIndex>(), any())
        } returns FlpsScore.of(0.75)
    }
}
