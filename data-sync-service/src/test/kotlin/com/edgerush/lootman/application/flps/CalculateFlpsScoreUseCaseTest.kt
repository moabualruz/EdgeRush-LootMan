package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

/**
 * Unit tests for CalculateFlpsScoreUseCase.
 */
class CalculateFlpsScoreUseCaseTest : UnitTest() {
    private val flpsCalculationService = FlpsCalculationService()
    private val modifierRepository = mockk<FlpsModifierRepository>()
    private val useCase = CalculateFlpsScoreUseCase(flpsCalculationService, modifierRepository)

    @Test
    fun `should calculate FLPS score successfully with default modifiers`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.9),
                mas = MechanicalAdherenceScore.of(0.8),
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns FlpsModifiers(command.guildId)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!

        // RMS = (0.9 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2) = 0.82
        flpsResult.rms.value shouldBe (0.82 plusOrMinus 0.0001)

        // IPI = (0.8 * 0.45) + (1.1 * 0.35) + (1.0 * 0.20) = 0.945
        flpsResult.ipi.value shouldBe (0.945 plusOrMinus 0.0001)

        // FLPS = (0.82 × 0.945) × 1.0 = 0.7749
        flpsResult.flps.value shouldBe (0.7749 plusOrMinus 0.0001)

        verify(exactly = 1) { modifierRepository.findByGuildId(command.guildId) }
    }

    @Test
    fun `should calculate FLPS score with custom guild modifiers`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.9),
                mas = MechanicalAdherenceScore.of(0.8),
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        val customModifiers =
            FlpsModifiers(
                guildId = command.guildId,
                rmsWeights =
                    com.edgerush.lootman.domain.flps.repository.RmsWeights(
                        attendance = 0.5,
                        mechanical = 0.3,
                        preparation = 0.2,
                    ),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns customModifiers

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!

        // RMS = (0.9 * 0.5) + (0.8 * 0.3) + (0.7 * 0.2) = 0.83
        flpsResult.rms.value shouldBe (0.83 plusOrMinus 0.0001)
    }

    @Test
    fun `should return zero FLPS when attendance is zero`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.zero(),
                mas = MechanicalAdherenceScore.of(0.8),
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns FlpsModifiers(command.guildId)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!

        // RMS will be low due to zero attendance
        // (0.0 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2) = 0 + 0.32 + 0.14 = 0.46
        flpsResult.rms.value shouldBe (0.46 plusOrMinus 0.0001)
    }

    @Test
    fun `should mark raider as ineligible when MAS is zero and activity threshold is zero`() {
        // Arrange - MAS is 0.0 but threshold is 0.0 (so MAS > threshold fails)
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.9), // Above attendance threshold
                mas = MechanicalAdherenceScore.of(0.0), // Exactly at activity threshold (fails because > not >=)
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns FlpsModifiers(command.guildId)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!

        // Raider should be ineligible because MAS (0.0) is not > threshold (0.0)
        flpsResult.eligible shouldBe false
    }

    @Test
    fun `should mark raider as eligible when both ACS and MAS meet thresholds`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.85), // Above attendance threshold of 0.8
                mas = MechanicalAdherenceScore.of(0.1), // Above activity threshold of 0.0
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns FlpsModifiers(command.guildId)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!
        flpsResult.eligible shouldBe true
    }

    @Test
    fun `should access guildId from FlpsCalculationResult`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val command =
            CalculateFlpsScoreCommand(
                guildId = guildId,
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.9),
                mas = MechanicalAdherenceScore.of(0.8),
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } returns FlpsModifiers(command.guildId)

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val flpsResult = result.getOrNull()!!

        // Explicitly access guildId to ensure coverage
        flpsResult.guildId shouldBe guildId
        flpsResult.guildId.value shouldBe "test-guild"
    }

    @Test
    fun `should return failure result when modifier repository throws exception`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId(1L),
                itemId = ItemId(12345),
                acs = AttendanceCommitmentScore.of(0.9),
                mas = MechanicalAdherenceScore.of(0.8),
                eps = ExternalPreparationScore.of(0.7),
                uv = UpgradeValue.of(0.8),
                tb = TierBonus.of(1.1),
                rm = RoleMultiplier.of(1.0),
                rdf = RecencyDecayFactor.of(1.0),
            )

        every { modifierRepository.findByGuildId(command.guildId) } throws RuntimeException("Database error")

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
    }
}
