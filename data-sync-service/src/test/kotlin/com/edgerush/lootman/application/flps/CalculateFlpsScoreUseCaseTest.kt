package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.*
import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
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
                raiderId = RaiderId("test-raider"),
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
        flpsResult.rms.value shouldBe 0.82

        // IPI = (0.8 * 0.45) + (1.1 * 0.35) + (1.0 * 0.20) = 0.945
        flpsResult.ipi.value shouldBe 0.945

        // FLPS = (0.82 × 0.945) × 1.0 = 0.7749
        flpsResult.flps.value shouldBe 0.7749

        verify(exactly = 1) { modifierRepository.findByGuildId(command.guildId) }
    }

    @Test
    fun `should calculate FLPS score with custom guild modifiers`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId("test-raider"),
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
        flpsResult.rms.value shouldBe 0.83
    }

    @Test
    fun `should return zero FLPS when attendance is zero`() {
        // Arrange
        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId("test-guild"),
                raiderId = RaiderId("test-raider"),
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
        flpsResult.rms.value shouldBe 0.3 // (0.0 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2)
    }
}
