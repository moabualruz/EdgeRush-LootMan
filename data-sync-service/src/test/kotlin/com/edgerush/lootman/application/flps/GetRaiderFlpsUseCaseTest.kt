package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.datasync.test.fixtures.RaiderFixtures
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.model.RaiderPreparationData
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for GetRaiderFlpsUseCase.
 *
 * Tests the orchestration of FLPS calculation for a specific raider and item.
 */
class GetRaiderFlpsUseCaseTest : UnitTest() {
    @MockK
    private lateinit var flpsDataAssembler: FlpsDataAssemblerService

    @MockK
    private lateinit var componentCalculator: FlpsComponentCalculator

    @MockK
    private lateinit var calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase

    @InjectMockKs
    private lateinit var useCase: GetRaiderFlpsUseCase

    @Nested
    inner class ExecuteQuery {
        @Test
        fun `should return FLPS result when raider is found`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiderId = RaiderId(42L)
            val itemId = ItemId(100L)
            val query = GetRaiderFlpsQuery(guildId, raiderId, itemId)

            val raider = createTestRaider(id = 42L, guildId = "test-guild")
            val raiderData = createTestRaiderData(raider)

            every { flpsDataAssembler.assembleFlpsData(guildId) } returns listOf(raiderData)
            setupComponentCalculatorMocks()
            setupCalculationUseCaseMock(guildId, raiderId, itemId, flpsValue = 0.85)

            // Act
            val result = useCase.execute(query)

            // Assert
            result.isSuccess shouldBe true
            result.getOrNull()!!.flps.value shouldBe 0.85
        }

        @Test
        fun `should return failure when raider is not found`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiderId = RaiderId(999L)
            val itemId = ItemId(100L)
            val query = GetRaiderFlpsQuery(guildId, raiderId, itemId)

            // Return empty list - no raiders match
            every { flpsDataAssembler.assembleFlpsData(guildId) } returns emptyList()

            // Act
            val result = useCase.execute(query)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
        }

        @Test
        fun `should return failure when raider exists but with different ID`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiderId = RaiderId(999L) // Looking for 999
            val itemId = ItemId(100L)
            val query = GetRaiderFlpsQuery(guildId, raiderId, itemId)

            val raider = createTestRaider(id = 42L, guildId = "test-guild") // But have 42
            val raiderData = createTestRaiderData(raider)

            every { flpsDataAssembler.assembleFlpsData(guildId) } returns listOf(raiderData)

            // Act
            val result = useCase.execute(query)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message shouldBe "Raider not found with id: 999"
        }

        @Test
        fun `should pass correct command to calculate use case`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiderId = RaiderId(42L)
            val itemId = ItemId(100L)
            val query = GetRaiderFlpsQuery(guildId, raiderId, itemId)

            val raider = createTestRaider(id = 42L, guildId = "test-guild")
            val raiderData = createTestRaiderData(raider)

            every { flpsDataAssembler.assembleFlpsData(guildId) } returns listOf(raiderData)
            setupComponentCalculatorMocks()

            val commandSlot = slot<CalculateFlpsScoreCommand>()
            every { calculateFlpsScoreUseCase.execute(capture(commandSlot)) } returns
                Result.success(createTestFlpsResult(guildId.value, raiderId.value, itemId.value))

            // Act
            useCase.execute(query)

            // Assert
            val capturedCommand = commandSlot.captured
            capturedCommand.guildId shouldBe guildId
            capturedCommand.raiderId shouldBe raiderId
            capturedCommand.itemId shouldBe itemId
        }

        @Test
        fun `should propagate exception from data assembler`() {
            // Arrange
            val query =
                GetRaiderFlpsQuery(
                    guildId = GuildId("test-guild"),
                    raiderId = RaiderId(42L),
                    itemId = ItemId(100L),
                )

            every { flpsDataAssembler.assembleFlpsData(any()) } throws RuntimeException("Database error")

            // Act
            val result = useCase.execute(query)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message shouldBe "Database error"
        }

        @Test
        fun `should propagate exception from calculate use case`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiderId = RaiderId(42L)
            val itemId = ItemId(100L)
            val query = GetRaiderFlpsQuery(guildId, raiderId, itemId)

            val raider = createTestRaider(id = 42L, guildId = "test-guild")
            val raiderData = createTestRaiderData(raider)

            every { flpsDataAssembler.assembleFlpsData(guildId) } returns listOf(raiderData)
            setupComponentCalculatorMocks()
            every { calculateFlpsScoreUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Calculation failed"))

            // Act
            val result = useCase.execute(query)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message shouldBe "Calculation failed"
        }
    }

    // Helper functions

    private fun createTestRaider(
        id: Long = 1L,
        guildId: String = "test-guild",
        name: String = "TestRaider",
    ): Raider =
        RaiderFixtures.createRaider(
            id = RaiderId(id),
            guildId = GuildId(guildId),
            name = name,
            realm = "TestRealm",
            characterClass = CharacterClass.WARRIOR,
            role = Role.DPS,
            rank = "Raider",
            status = RaiderStatus.ACTIVE,
            joinDate = LocalDateTime.now(),
            wowauditId = id,
        )

    private fun createTestRaiderData(raider: Raider): RaiderFlpsData =
        RaiderFlpsData(
            raider = raider,
            attendance = emptyList(),
            lootHistory = emptyList(),
            wishlist = null,
            gear = null,
            activeBans = emptyList(),
            performanceData = RaiderPerformanceData.empty(raider.id, raider.characterName, raider.realm),
            preparation = RaiderPreparationData.empty(raider.id),
        )

    private fun setupComponentCalculatorMocks() {
        every { componentCalculator.calculateACS(any()) } returns AttendanceCommitmentScore.of(0.9)
        every { componentCalculator.calculateMAS(any()) } returns MechanicalAdherenceScore.of(0.8)
        every { componentCalculator.calculateEPS(any(), any()) } returns ExternalPreparationScore.of(0.7)
        every { componentCalculator.calculateUV(any(), any()) } returns UpgradeValue.of(0.6)
        every { componentCalculator.calculateTierBonus(any()) } returns TierBonus.of(0.5)
        every { componentCalculator.calculateRoleMultiplier(any()) } returns RoleMultiplier.of(1.0)
        every { componentCalculator.calculateRDF(any(), any()) } returns RecencyDecayFactor.of(0.95)
    }

    private fun setupCalculationUseCaseMock(
        guildId: GuildId,
        raiderId: RaiderId,
        itemId: ItemId,
        flpsValue: Double = 0.80,
    ) {
        val result = createTestFlpsResult(guildId.value, raiderId.value, itemId.value, flpsValue)
        every { calculateFlpsScoreUseCase.execute(any()) } returns Result.success(result)
    }

    private fun createTestFlpsResult(
        guildId: String = "test-guild",
        raiderId: Long = 1L,
        itemId: Long = 100L,
        flpsValue: Double = 0.80,
    ): FlpsCalculationResult =
        FlpsCalculationResult(
            guildId = GuildId(guildId),
            raiderId = RaiderId(raiderId),
            itemId = ItemId(itemId),
            acs = AttendanceCommitmentScore.of(0.9),
            mas = MechanicalAdherenceScore.of(0.8),
            eps = ExternalPreparationScore.of(0.7),
            rms = RaiderMeritScore.of(0.85),
            uv = UpgradeValue.of(0.6),
            tb = TierBonus.of(0.5),
            rm = RoleMultiplier.of(1.0),
            ipi = ItemPriorityIndex.of(0.65),
            rdf = RecencyDecayFactor.of(0.95),
            flps = FlpsScore.of(flpsValue),
            eligible = true,
        )
}
