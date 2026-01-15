package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsReport
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for FLPS response DTO classes.
 *
 * These tests ensure all response DTO properties are properly accessible,
 * including nested component structures and factory methods.
 */
class FlpsResponseDtoTest : UnitTest() {
    @Nested
    inner class RmsComponentsResponseTest {
        @Test
        fun `should create response with all RMS components`() {
            // Given
            val acs = 0.9
            val mas = 0.8
            val eps = 0.7
            val total = 0.82

            // When
            val response =
                RmsComponentsResponse(
                    acs = acs,
                    mas = mas,
                    eps = eps,
                    total = total,
                )

            // Then
            response.acs shouldBe acs
            response.mas shouldBe mas
            response.eps shouldBe eps
            response.total shouldBe total
        }

        @Test
        fun `should have correct property access for minimum values`() {
            // Given
            val response =
                RmsComponentsResponse(
                    acs = 0.0,
                    mas = 0.0,
                    eps = 0.0,
                    total = 0.0,
                )

            // Then
            response.acs shouldBe 0.0
            response.mas shouldBe 0.0
            response.eps shouldBe 0.0
            response.total shouldBe 0.0
        }

        @Test
        fun `should have correct property access for maximum values`() {
            // Given
            val response =
                RmsComponentsResponse(
                    acs = 1.0,
                    mas = 1.0,
                    eps = 1.0,
                    total = 1.0,
                )

            // Then
            response.acs shouldBe 1.0
            response.mas shouldBe 1.0
            response.eps shouldBe 1.0
            response.total shouldBe 1.0
        }
    }

    @Nested
    inner class IpiComponentsResponseTest {
        @Test
        fun `should create response with all IPI components`() {
            // Given
            val uv = 0.8
            val tb = 1.1
            val rm = 1.0
            val total = 0.945

            // When
            val response =
                IpiComponentsResponse(
                    uv = uv,
                    tb = tb,
                    rm = rm,
                    total = total,
                )

            // Then
            response.uv shouldBe uv
            response.tb shouldBe tb
            response.rm shouldBe rm
            response.total shouldBe total
        }

        @Test
        fun `should have correct property access for varying values`() {
            // Given
            val response =
                IpiComponentsResponse(
                    uv = 0.65,
                    tb = 1.2,
                    rm = 1.15,
                    total = 0.88,
                )

            // Then
            response.uv shouldBe 0.65
            response.tb shouldBe 1.2
            response.rm shouldBe 1.15
            response.total shouldBe 0.88
        }

        @Test
        fun `should support data class copy operation`() {
            // Given
            val original =
                IpiComponentsResponse(
                    uv = 0.5,
                    tb = 1.0,
                    rm = 1.0,
                    total = 0.5,
                )

            // When
            val copy = original.copy(uv = 0.9, total = 0.9)

            // Then
            copy.uv shouldBe 0.9
            copy.tb shouldBe original.tb
            copy.rm shouldBe original.rm
            copy.total shouldBe 0.9
        }
    }

    @Nested
    inner class FlpsComponentsResponseTest {
        @Test
        fun `should create response with all component groups`() {
            // Given
            val rms =
                RmsComponentsResponse(
                    acs = 0.9,
                    mas = 0.8,
                    eps = 0.7,
                    total = 0.82,
                )
            val ipi =
                IpiComponentsResponse(
                    uv = 0.8,
                    tb = 1.1,
                    rm = 1.0,
                    total = 0.945,
                )
            val rdf = 1.0

            // When
            val response =
                FlpsComponentsResponse(
                    rms = rms,
                    ipi = ipi,
                    rdf = rdf,
                )

            // Then
            response.rms shouldBe rms
            response.ipi shouldBe ipi
            response.rdf shouldBe rdf
        }

        @Test
        fun `should have correct nested property access`() {
            // Given
            val response =
                FlpsComponentsResponse(
                    rms = RmsComponentsResponse(0.85, 0.75, 0.65, 0.78),
                    ipi = IpiComponentsResponse(0.7, 1.05, 1.1, 0.82),
                    rdf = 0.95,
                )

            // Then
            response.rms.acs shouldBe 0.85
            response.rms.mas shouldBe 0.75
            response.rms.eps shouldBe 0.65
            response.rms.total shouldBe 0.78
            response.ipi.uv shouldBe 0.7
            response.ipi.tb shouldBe 1.05
            response.ipi.rm shouldBe 1.1
            response.ipi.total shouldBe 0.82
            response.rdf shouldBe 0.95
        }

        @Test
        fun `should support data class copy operation`() {
            // Given
            val original =
                FlpsComponentsResponse(
                    rms = RmsComponentsResponse(0.5, 0.5, 0.5, 0.5),
                    ipi = IpiComponentsResponse(0.5, 1.0, 1.0, 0.5),
                    rdf = 1.0,
                )

            // When
            val copy = original.copy(rdf = 0.8)

            // Then
            copy.rms shouldBe original.rms
            copy.ipi shouldBe original.ipi
            copy.rdf shouldBe 0.8
        }
    }

    @Nested
    inner class FlpsCalculationResponseTest {
        @Test
        fun `should create response with all properties`() {
            // Given
            val raiderId = "raider-456"
            val itemId = 12345L
            val components =
                FlpsComponentsResponse(
                    rms = RmsComponentsResponse(0.9, 0.8, 0.7, 0.82),
                    ipi = IpiComponentsResponse(0.8, 1.1, 1.0, 0.945),
                    rdf = 1.0,
                )
            val flpsScore = 0.85
            val eligible = true

            // When
            val response =
                FlpsCalculationResponse(
                    raiderId = raiderId,
                    itemId = itemId,
                    components = components,
                    flpsScore = flpsScore,
                    eligible = eligible,
                )

            // Then
            response.raiderId shouldBe raiderId
            response.itemId shouldBe itemId
            response.components shouldBe components
            response.flpsScore shouldBe flpsScore
            response.eligible shouldBe eligible
        }

        @Test
        fun `should have correct property access for ineligible calculation`() {
            // Given
            val response =
                FlpsCalculationResponse(
                    raiderId = "banned-raider",
                    itemId = 99999L,
                    components =
                        FlpsComponentsResponse(
                            rms = RmsComponentsResponse(0.1, 0.1, 0.1, 0.1),
                            ipi = IpiComponentsResponse(0.1, 1.0, 1.0, 0.1),
                            rdf = 0.5,
                        ),
                    flpsScore = 0.05,
                    eligible = false,
                )

            // Then
            response.raiderId shouldBe "banned-raider"
            response.itemId shouldBe 99999L
            response.components.rdf shouldBe 0.5
            response.flpsScore shouldBe 0.05
            response.eligible shouldBe false
        }

        @Test
        fun `should create from FlpsCalculationResult`() {
            // Given
            val result =
                FlpsCalculationResult(
                    guildId = GuildId("test-guild"),
                    raiderId = RaiderId(123L),
                    itemId = ItemId(456L),
                    acs = AttendanceCommitmentScore.of(0.9),
                    mas = MechanicalAdherenceScore.of(0.8),
                    eps = ExternalPreparationScore.of(0.7),
                    rms = RaiderMeritScore.of(0.82),
                    uv = UpgradeValue.of(0.8),
                    tb = TierBonus.of(1.1),
                    rm = RoleMultiplier.of(1.0),
                    ipi = ItemPriorityIndex.of(0.945),
                    rdf = RecencyDecayFactor.of(1.0),
                    flps = FlpsScore.of(0.85),
                    eligible = true,
                )

            // When
            val response = FlpsCalculationResponse.from(result)

            // Then
            response.raiderId shouldBe "123"
            response.itemId shouldBe 456L
            response.components.rms.acs shouldBe 0.9
            response.components.rms.mas shouldBe 0.8
            response.components.rms.eps shouldBe 0.7
            response.components.rms.total shouldBe 0.82
            response.components.ipi.uv shouldBe 0.8
            response.components.ipi.tb shouldBe 1.1
            response.components.ipi.rm shouldBe 1.0
            response.components.ipi.total shouldBe 0.945
            response.components.rdf shouldBe 1.0
            response.flpsScore shouldBe 0.85
            response.eligible shouldBe true
        }
    }

    @Nested
    inner class FlpsReportResponseTest {
        @Test
        fun `should create from FlpsReport with empty calculations`() {
            // Given
            val report =
                FlpsReport(
                    guildId = GuildId("test-guild"),
                    calculations = emptyList(),
                )

            // When
            val response = FlpsReportResponse.from(report)

            // Then
            response.guildId shouldBe "test-guild"
            response.calculations shouldHaveSize 0
        }

        @Test
        fun `should create from FlpsReport with multiple calculations`() {
            // Given
            val calculation1 = createCalculationResult(1L, 0.90)
            val calculation2 = createCalculationResult(2L, 0.75)
            val report =
                FlpsReport(
                    guildId = GuildId("multi-raider-guild"),
                    calculations = listOf(calculation1, calculation2),
                )

            // When
            val response = FlpsReportResponse.from(report)

            // Then
            response.guildId shouldBe "multi-raider-guild"
            response.calculations shouldHaveSize 2
            response.calculations[0].raiderId shouldBe "1"
            response.calculations[0].flpsScore shouldBe 0.90
            response.calculations[1].raiderId shouldBe "2"
            response.calculations[1].flpsScore shouldBe 0.75
        }

        @Test
        fun `should have correct property access`() {
            // Given
            val response =
                FlpsReportResponse(
                    guildId = "direct-guild",
                    calculations =
                        listOf(
                            FlpsCalculationResponse(
                                raiderId = "raider-1",
                                itemId = 100L,
                                components =
                                    FlpsComponentsResponse(
                                        rms = RmsComponentsResponse(0.9, 0.9, 0.9, 0.9),
                                        ipi = IpiComponentsResponse(0.9, 1.0, 1.0, 0.9),
                                        rdf = 1.0,
                                    ),
                                flpsScore = 0.95,
                                eligible = true,
                            ),
                        ),
                )

            // Then
            response.guildId shouldBe "direct-guild"
            response.calculations[0].raiderId shouldBe "raider-1"
            response.calculations[0].itemId shouldBe 100L
            response.calculations[0].components.rms.acs shouldBe 0.9
            response.calculations[0].eligible shouldBe true
        }

        private fun createCalculationResult(
            raiderId: Long,
            flpsScore: Double,
        ): FlpsCalculationResult {
            return FlpsCalculationResult(
                guildId = GuildId("test-guild"),
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
}
