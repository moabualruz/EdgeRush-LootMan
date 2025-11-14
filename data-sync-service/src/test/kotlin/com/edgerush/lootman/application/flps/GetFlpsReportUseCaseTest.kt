package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.*
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for GetFlpsReportUseCase.
 */
class GetFlpsReportUseCaseTest : UnitTest() {
    private val useCase = GetFlpsReportUseCase()

    @Test
    fun `should aggregate multiple FLPS calculations into report`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val calculations =
            listOf(
                FlpsCalculationResult(
                    guildId = guildId,
                    raiderId = RaiderId("raider1"),
                    itemId = ItemId(12345),
                    acs = AttendanceCommitmentScore.of(0.9),
                    mas = MechanicalAdherenceScore.of(0.8),
                    eps = ExternalPreparationScore.of(0.7),
                    rms = RaiderMeritScore.of(0.82),
                    uv = UpgradeValue.of(0.8),
                    tb = TierBonus.of(1.1),
                    rm = RoleMultiplier.of(1.0),
                    ipi = ItemPriorityIndex.of(0.945),
                    rdf = RecencyDecayFactor.of(1.0),
                    flps = FlpsScore.of(0.7749),
                    eligible = true,
                ),
                FlpsCalculationResult(
                    guildId = guildId,
                    raiderId = RaiderId("raider2"),
                    itemId = ItemId(12345),
                    acs = AttendanceCommitmentScore.of(0.85),
                    mas = MechanicalAdherenceScore.of(0.75),
                    eps = ExternalPreparationScore.of(0.65),
                    rms = RaiderMeritScore.of(0.77),
                    uv = UpgradeValue.of(0.7),
                    tb = TierBonus.of(1.0),
                    rm = RoleMultiplier.of(0.8),
                    ipi = ItemPriorityIndex.of(0.815),
                    rdf = RecencyDecayFactor.of(0.9),
                    flps = FlpsScore.of(0.565),
                    eligible = true,
                ),
            )

        val query = GetFlpsReportQuery(guildId, calculations)

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.guildId shouldBe guildId
        report.calculations shouldHaveSize 2
        report.calculations[0].flps.value shouldBe 0.7749
        report.calculations[1].flps.value shouldBe 0.565
    }

    @Test
    fun `should sort calculations by FLPS score descending`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val calculations =
            listOf(
                createCalculation(RaiderId("raider1"), FlpsScore.of(0.5)),
                createCalculation(RaiderId("raider2"), FlpsScore.of(0.9)),
                createCalculation(RaiderId("raider3"), FlpsScore.of(0.7)),
            )

        val query = GetFlpsReportQuery(guildId, calculations)

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.calculations[0].flps.value shouldBe 0.9
        report.calculations[1].flps.value shouldBe 0.7
        report.calculations[2].flps.value shouldBe 0.5
    }

    @Test
    fun `should handle empty calculations list`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val query = GetFlpsReportQuery(guildId, emptyList())

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.calculations shouldHaveSize 0
    }

    private fun createCalculation(
        raiderId: RaiderId,
        flps: FlpsScore,
    ): FlpsCalculationResult {
        return FlpsCalculationResult(
            guildId = GuildId("test-guild"),
            raiderId = raiderId,
            itemId = ItemId(12345),
            acs = AttendanceCommitmentScore.of(0.9),
            mas = MechanicalAdherenceScore.of(0.8),
            eps = ExternalPreparationScore.of(0.7),
            rms = RaiderMeritScore.of(0.82),
            uv = UpgradeValue.of(0.8),
            tb = TierBonus.of(1.1),
            rm = RoleMultiplier.of(1.0),
            ipi = ItemPriorityIndex.of(0.945),
            rdf = RecencyDecayFactor.of(1.0),
            flps = flps,
            eligible = true,
        )
    }
}
