package com.edgerush.lootman.api.graphql.query

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsReport
import com.edgerush.lootman.application.flps.GetFlpsReportUseCase
import com.edgerush.lootman.application.flps.GetRaiderFlpsQuery
import com.edgerush.lootman.application.flps.GetRaiderFlpsUseCase
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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for FlpsQueryResolver.
 *
 * Tests the GraphQL query resolver for FLPS operations following TDD principles.
 */
class FlpsQueryResolverTest : UnitTest() {
    @MockK
    private lateinit var getRaiderFlpsUseCase: GetRaiderFlpsUseCase

    @MockK
    private lateinit var getFlpsReportUseCase: GetFlpsReportUseCase

    @InjectMockKs
    private lateinit var resolver: FlpsQueryResolver

    @Nested
    inner class FlpsScoreQuery {
        @Test
        fun `should calculate and return FLPS score for raider and item`() {
            // Arrange
            val result =
                createTestFlpsResult(
                    guildId = "guild-123",
                    raiderId = 42L,
                    itemId = 100L,
                    flpsValue = 0.85,
                )
            val querySlot = slot<GetRaiderFlpsQuery>()
            every { getRaiderFlpsUseCase.execute(capture(querySlot)) } returns Result.success(result)

            // Act
            val flpsType =
                resolver.flpsScore(
                    guildId = "guild-123",
                    raiderId = "42",
                    itemId = "100",
                )

            // Assert
            flpsType.shouldNotBeNull()
            flpsType.value shouldBe 0.85
            flpsType.eligible shouldBe true
            querySlot.captured.guildId.value shouldBe "guild-123"
            querySlot.captured.raiderId.value shouldBe 42L
            querySlot.captured.itemId.value shouldBe 100L
        }

        @Test
        fun `should return null when calculation fails with NotFound`() {
            // Arrange
            every { getRaiderFlpsUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Raider not found"))

            // Act
            val result =
                resolver.flpsScore(
                    guildId = "guild-123",
                    raiderId = "999",
                    itemId = "100",
                )

            // Assert
            result.shouldBeNull()
        }

        @Test
        fun `should propagate exception for non-NotFound errors`() {
            // Arrange
            every { getRaiderFlpsUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.flpsScore(
                        guildId = "guild-123",
                        raiderId = "42",
                        itemId = "100",
                    )
                }
            exception.message shouldBe "Database error"
        }

        @Test
        fun `should include full breakdown in FLPS score type`() {
            // Arrange
            val result =
                createTestFlpsResult(
                    acsValue = 0.95,
                    masValue = 0.88,
                    epsValue = 0.75,
                    rmsValue = 0.86,
                    uvValue = 0.60,
                    tbValue = 0.80,
                    rmValue = 1.0,
                    ipiValue = 0.70,
                    rdfValue = 0.92,
                    flpsValue = 0.82,
                )
            every { getRaiderFlpsUseCase.execute(any()) } returns Result.success(result)

            // Act
            val flpsType =
                resolver.flpsScore(
                    guildId = "guild-123",
                    raiderId = "42",
                    itemId = "100",
                )

            // Assert
            flpsType.shouldNotBeNull()
            flpsType.breakdown.shouldNotBeNull()
            flpsType.breakdown.acs shouldBe 0.95
            flpsType.breakdown.mas shouldBe 0.88
            flpsType.breakdown.eps shouldBe 0.75
            flpsType.breakdown.rms shouldBe 0.86
            flpsType.breakdown.uv shouldBe 0.60
            flpsType.breakdown.tb shouldBe 0.80
            flpsType.breakdown.rm shouldBe 1.0
            flpsType.breakdown.ipi shouldBe 0.70
            flpsType.breakdown.rdf shouldBe 0.92
        }
    }

    @Nested
    inner class FlpsReportQuery {
        @Test
        fun `should return FLPS report for guild`() {
            // Arrange
            val calculations =
                listOf(
                    createTestFlpsResult(raiderId = 1L, flpsValue = 0.90),
                    createTestFlpsResult(raiderId = 2L, flpsValue = 0.85),
                    createTestFlpsResult(raiderId = 3L, flpsValue = 0.75),
                )
            val report =
                FlpsReport(
                    guildId = GuildId("guild-123"),
                    calculations = calculations,
                )
            every { getFlpsReportUseCase.execute(any()) } returns Result.success(report)

            // Act
            val result = resolver.flpsReport(guildId = "guild-123")

            // Assert
            result.shouldNotBeNull()
            result.guildId shouldBe "guild-123"
            result.scores shouldHaveSize 3
            result.scores[0].value shouldBe 0.90
            result.scores[1].value shouldBe 0.85
            result.scores[2].value shouldBe 0.75
        }

        @Test
        fun `should return empty report when no calculations exist`() {
            // Arrange
            val report =
                FlpsReport(
                    guildId = GuildId("guild-123"),
                    calculations = emptyList(),
                )
            every { getFlpsReportUseCase.execute(any()) } returns Result.success(report)

            // Act
            val result = resolver.flpsReport(guildId = "guild-123")

            // Assert
            result.shouldNotBeNull()
            result.scores shouldHaveSize 0
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { getFlpsReportUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database connection failed"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.flpsReport(guildId = "guild-123")
                }
            exception.message shouldBe "Database connection failed"
        }

        @Test
        fun `should include raider information in report scores`() {
            // Arrange
            val calculations =
                listOf(
                    createTestFlpsResult(raiderId = 42L, flpsValue = 0.88, eligible = true),
                )
            val report =
                FlpsReport(
                    guildId = GuildId("guild-123"),
                    calculations = calculations,
                )
            every { getFlpsReportUseCase.execute(any()) } returns Result.success(report)

            // Act
            val result = resolver.flpsReport(guildId = "guild-123")

            // Assert
            result.shouldNotBeNull()
            result.scores shouldHaveSize 1
            result.scores[0].raiderId shouldBe "42"
            result.scores[0].itemId shouldBe "100"
            result.scores[0].eligible shouldBe true
        }
    }

    // Helper function to create test FLPS calculation results
    private fun createTestFlpsResult(
        guildId: String = "guild-123",
        raiderId: Long = 1L,
        itemId: Long = 100L,
        acsValue: Double = 0.90,
        masValue: Double = 0.85,
        epsValue: Double = 0.80,
        rmsValue: Double = 0.85,
        uvValue: Double = 0.50,
        tbValue: Double = 0.70,
        rmValue: Double = 1.0,
        ipiValue: Double = 0.65,
        rdfValue: Double = 0.95,
        flpsValue: Double = 0.80,
        eligible: Boolean = true,
    ): FlpsCalculationResult =
        FlpsCalculationResult(
            guildId = GuildId(guildId),
            raiderId = RaiderId(raiderId),
            itemId = ItemId(itemId),
            acs = AttendanceCommitmentScore.of(acsValue),
            mas = MechanicalAdherenceScore.of(masValue),
            eps = ExternalPreparationScore.of(epsValue),
            rms = RaiderMeritScore.of(rmsValue),
            uv = UpgradeValue.of(uvValue),
            tb = TierBonus.of(tbValue),
            rm = RoleMultiplier.of(rmValue),
            ipi = ItemPriorityIndex.of(ipiValue),
            rdf = RecencyDecayFactor.of(rdfValue),
            flps = FlpsScore.of(flpsValue),
            eligible = eligible,
        )
}
