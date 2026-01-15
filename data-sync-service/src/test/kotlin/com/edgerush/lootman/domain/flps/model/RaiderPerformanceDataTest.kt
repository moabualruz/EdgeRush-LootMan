package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for RaiderPerformanceData value object.
 */
class RaiderPerformanceDataTest : UnitTest() {
    @Nested
    inner class ConstructorValidationTests {
        @Test
        fun `should create with valid performance metrics`() {
            // Given
            val raiderId = RaiderId(1L)
            val periodStart = Instant.now().minusSeconds(86400)
            val periodEnd = Instant.now()

            // When
            val data =
                RaiderPerformanceData(
                    raiderId = raiderId,
                    characterName = "TestChar",
                    characterRealm = "Area52",
                    totalDeaths = 5,
                    totalFights = 20,
                    deathsPerAttempt = 0.25,
                    avoidableDamagePercentage = 15.5,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                )

            // Then
            data.raiderId shouldBe raiderId
            data.characterName shouldBe "TestChar"
            data.characterRealm shouldBe "Area52"
            data.totalDeaths shouldBe 5
            data.totalFights shouldBe 20
            data.deathsPerAttempt shouldBe 0.25
            data.avoidableDamagePercentage shouldBe 15.5
            data.periodStart shouldBe periodStart
            data.periodEnd shouldBe periodEnd
        }

        @Test
        fun `should fail when total deaths is negative`() {
            // When/Then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaiderPerformanceData(
                        raiderId = RaiderId(1L),
                        characterName = "TestChar",
                        characterRealm = "Area52",
                        totalDeaths = -1,
                        totalFights = 10,
                        deathsPerAttempt = 0.0,
                        avoidableDamagePercentage = 10.0,
                        periodStart = Instant.now(),
                        periodEnd = Instant.now(),
                    )
                }
            exception.message shouldBe "Total deaths cannot be negative"
        }

        @Test
        fun `should fail when total fights is negative`() {
            // When/Then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaiderPerformanceData(
                        raiderId = RaiderId(1L),
                        characterName = "TestChar",
                        characterRealm = "Area52",
                        totalDeaths = 0,
                        totalFights = -1,
                        deathsPerAttempt = 0.0,
                        avoidableDamagePercentage = 10.0,
                        periodStart = Instant.now(),
                        periodEnd = Instant.now(),
                    )
                }
            exception.message shouldBe "Total fights cannot be negative"
        }

        @Test
        fun `should fail when deaths per attempt is negative`() {
            // When/Then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaiderPerformanceData(
                        raiderId = RaiderId(1L),
                        characterName = "TestChar",
                        characterRealm = "Area52",
                        totalDeaths = 0,
                        totalFights = 10,
                        deathsPerAttempt = -0.5,
                        avoidableDamagePercentage = 10.0,
                        periodStart = Instant.now(),
                        periodEnd = Instant.now(),
                    )
                }
            exception.message shouldBe "Deaths per attempt cannot be negative"
        }

        @Test
        fun `should fail when avoidable damage percentage is negative`() {
            // When/Then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaiderPerformanceData(
                        raiderId = RaiderId(1L),
                        characterName = "TestChar",
                        characterRealm = "Area52",
                        totalDeaths = 0,
                        totalFights = 10,
                        deathsPerAttempt = 0.0,
                        avoidableDamagePercentage = -5.0,
                        periodStart = Instant.now(),
                        periodEnd = Instant.now(),
                    )
                }
            exception.message shouldBe "Avoidable damage percentage cannot be negative"
        }

        @Test
        fun `should allow zero values for all metrics`() {
            // When
            val data =
                RaiderPerformanceData(
                    raiderId = RaiderId(1L),
                    characterName = "TestChar",
                    characterRealm = "Area52",
                    totalDeaths = 0,
                    totalFights = 0,
                    deathsPerAttempt = 0.0,
                    avoidableDamagePercentage = 0.0,
                    periodStart = Instant.now(),
                    periodEnd = Instant.now(),
                )

            // Then
            data.totalDeaths shouldBe 0
            data.totalFights shouldBe 0
            data.deathsPerAttempt shouldBe 0.0
            data.avoidableDamagePercentage shouldBe 0.0
        }
    }

    @Nested
    inner class FactoryMethodTests {
        @Test
        fun `create should calculate deaths per attempt correctly`() {
            // Given
            val periodStart = Instant.now().minusSeconds(86400)
            val periodEnd = Instant.now()

            // When
            val data =
                RaiderPerformanceData.create(
                    raiderId = RaiderId(1L),
                    characterName = "TestChar",
                    characterRealm = "Area52",
                    totalDeaths = 10,
                    totalFights = 20,
                    avoidableDamagePercentage = 25.0,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                )

            // Then
            data.deathsPerAttempt shouldBe (0.5 plusOrMinus 0.001)
            data.totalDeaths shouldBe 10
            data.totalFights shouldBe 20
        }

        @Test
        fun `create should handle zero fights gracefully`() {
            // When
            val data =
                RaiderPerformanceData.create(
                    raiderId = RaiderId(1L),
                    characterName = "TestChar",
                    characterRealm = "Area52",
                    totalDeaths = 0,
                    totalFights = 0,
                    avoidableDamagePercentage = 0.0,
                    periodStart = Instant.now(),
                    periodEnd = Instant.now(),
                )

            // Then
            data.deathsPerAttempt shouldBe 0.0
            data.totalFights shouldBe 0
        }

        @Test
        fun `empty should create data with zero metrics`() {
            // When
            val data =
                RaiderPerformanceData.empty(
                    raiderId = RaiderId(1L),
                    characterName = "TestChar",
                    characterRealm = "Area52",
                )

            // Then
            data.raiderId.value shouldBe 1L
            data.characterName shouldBe "TestChar"
            data.characterRealm shouldBe "Area52"
            data.totalDeaths shouldBe 0
            data.totalFights shouldBe 0
            data.deathsPerAttempt shouldBe 0.0
            data.avoidableDamagePercentage shouldBe 0.0
        }
    }
}
