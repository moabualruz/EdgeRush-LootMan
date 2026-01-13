package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
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
                    raiderId = RaiderId(1L),
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
                    raiderId = RaiderId(2L),
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
                createCalculation(RaiderId(1L), FlpsScore.of(0.5)),
                createCalculation(RaiderId(2L), FlpsScore.of(0.9)),
                createCalculation(RaiderId(3L), FlpsScore.of(0.7)),
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

    @Test
    fun `should access all properties of FlpsReport`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val calculations =
            listOf(
                createCalculation(RaiderId(1L), FlpsScore.of(0.8)),
            )

        val query = GetFlpsReportQuery(guildId, calculations)

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        // Access all properties explicitly to ensure coverage
        report.guildId shouldBe guildId
        report.guildId.value shouldBe "test-guild"
        report.calculations shouldHaveSize 1
        report.calculations[0].raiderId.value shouldBe 1L
    }

    @Test
    fun `should handle single calculation in report`() {
        // Arrange
        val guildId = GuildId("single-calc-guild")
        val calculation = createCalculation(RaiderId(1L), FlpsScore.of(0.75))
        val query = GetFlpsReportQuery(guildId, listOf(calculation))

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.guildId shouldBe guildId
        report.calculations shouldHaveSize 1
        report.calculations[0].flps.value shouldBe 0.75
    }

    @Test
    fun `should preserve all calculation details in report`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val calculation =
            FlpsCalculationResult(
                guildId = guildId,
                raiderId = RaiderId(1L),
                itemId = ItemId(99999),
                acs = AttendanceCommitmentScore.of(0.95),
                mas = MechanicalAdherenceScore.of(0.85),
                eps = ExternalPreparationScore.of(0.75),
                rms = RaiderMeritScore.of(0.88),
                uv = UpgradeValue.of(0.9),
                tb = TierBonus.of(1.2),
                rm = RoleMultiplier.of(1.1),
                ipi = ItemPriorityIndex.of(0.99),
                rdf = RecencyDecayFactor.of(0.95),
                flps = FlpsScore.of(0.85),
                eligible = false,
            )

        val query = GetFlpsReportQuery(guildId, listOf(calculation))

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!
        val reportedCalc = report.calculations[0]

        // Verify all properties are preserved
        reportedCalc.guildId shouldBe guildId
        reportedCalc.raiderId.value shouldBe 1L
        reportedCalc.itemId.value shouldBe 99999
        reportedCalc.acs.value shouldBe 0.95
        reportedCalc.mas.value shouldBe 0.85
        reportedCalc.eps.value shouldBe 0.75
        reportedCalc.rms.value shouldBe 0.88
        reportedCalc.uv.value shouldBe 0.9
        reportedCalc.tb.value shouldBe 1.2
        reportedCalc.rm.value shouldBe 1.1
        reportedCalc.ipi.value shouldBe 0.99
        reportedCalc.rdf.value shouldBe 0.95
        reportedCalc.flps.value shouldBe 0.85
        reportedCalc.eligible shouldBe false
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
