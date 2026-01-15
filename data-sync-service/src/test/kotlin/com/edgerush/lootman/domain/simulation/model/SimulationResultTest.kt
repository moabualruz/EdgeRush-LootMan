package com.edgerush.lootman.domain.simulation.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SimulationResultTest : UnitTest() {
    @Nested
    inner class Creation {
        @Test
        fun `should create SimulationResult with valid data`() {
            // Arrange
            val itemId = 12345L
            val itemName = "Fyrakk's Tainted Rageheart"
            val slot = "trinket1"
            val dpsGain = 5000.0
            val percentGain = 3.5
            val simulatedAt = Instant.now()

            // Act
            val result =
                SimulationResult.create(
                    itemId = itemId,
                    itemName = itemName,
                    slot = slot,
                    dpsGain = dpsGain,
                    percentGain = percentGain,
                    simulatedAt = simulatedAt,
                )

            // Assert
            result.itemId shouldBe itemId
            result.itemName shouldBe itemName
            result.slot shouldBe slot
            result.dpsGain shouldBe dpsGain
            result.percentGain shouldBe percentGain
            result.simulatedAt shouldBe simulatedAt
        }

        @Test
        fun `should allow zero dpsGain`() {
            // Act
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 0.0,
                    percentGain = 0.0,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result.dpsGain shouldBe 0.0
            result.percentGain shouldBe 0.0
        }

        @Test
        fun `should allow negative dpsGain for downgrades`() {
            // Act
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Worse Item",
                    slot = "head",
                    dpsGain = -1500.0,
                    percentGain = -1.2,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result.dpsGain shouldBe -1500.0
            result.percentGain shouldBe -1.2
            result.isUpgrade shouldBe false
        }

        @Test
        fun `should throw exception when itemId is negative`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationResult.create(
                        itemId = -1L,
                        itemName = "Test Item",
                        slot = "head",
                        dpsGain = 1000.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    )
                }
            exception.message shouldContain "itemId"
        }

        @Test
        fun `should throw exception when itemName is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationResult.create(
                        itemId = 12345L,
                        itemName = "  ",
                        slot = "head",
                        dpsGain = 1000.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    )
                }
            exception.message shouldContain "itemName"
        }

        @Test
        fun `should throw exception when slot is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationResult.create(
                        itemId = 12345L,
                        itemName = "Test Item",
                        slot = "",
                        dpsGain = 1000.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    )
                }
            exception.message shouldContain "slot"
        }
    }

    @Nested
    inner class IsUpgrade {
        @Test
        fun `should return true when dpsGain is positive`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result.isUpgrade shouldBe true
        }

        @Test
        fun `should return false when dpsGain is zero`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 0.0,
                    percentGain = 0.0,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result.isUpgrade shouldBe false
        }

        @Test
        fun `should return false when dpsGain is negative`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = -500.0,
                    percentGain = -0.5,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result.isUpgrade shouldBe false
        }
    }

    @Nested
    inner class NormalizedUpgradeValue {
        @Test
        fun `should normalize positive gain to 0-1 range`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 5000.0,
                    percentGain = 5.0, // 5% gain
                    simulatedAt = Instant.now(),
                )

            // Act
            val normalized = result.normalizedUpgradeValue(maxPercentGain = 10.0)

            // Assert
            normalized shouldBe 0.5 // 5% / 10% = 0.5
        }

        @Test
        fun `should clamp normalized value to 1-0 when exceeds max`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 15000.0,
                    percentGain = 15.0, // 15% gain
                    simulatedAt = Instant.now(),
                )

            // Act
            val normalized = result.normalizedUpgradeValue(maxPercentGain = 10.0)

            // Assert
            normalized shouldBe 1.0 // Clamped to 1.0
        }

        @Test
        fun `should return 0 for negative gains`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = -500.0,
                    percentGain = -0.5,
                    simulatedAt = Instant.now(),
                )

            // Act
            val normalized = result.normalizedUpgradeValue(maxPercentGain = 10.0)

            // Assert
            normalized shouldBe 0.0
        }

        @Test
        fun `should use default max percent gain of 10 percent`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 5000.0,
                    percentGain = 5.0,
                    simulatedAt = Instant.now(),
                )

            // Act
            val normalized = result.normalizedUpgradeValue()

            // Assert
            normalized shouldBe 0.5
        }
    }

    @Nested
    inner class Comparison {
        @Test
        fun `should compare results by dpsGain`() {
            // Arrange
            val result1 =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Lesser Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now(),
                )
            val result2 =
                SimulationResult.create(
                    itemId = 12346L,
                    itemName = "Greater Item",
                    slot = "head",
                    dpsGain = 5000.0,
                    percentGain = 5.0,
                    simulatedAt = Instant.now(),
                )

            // Assert
            result1.dpsGain shouldBeLessThan result2.dpsGain
            result2.dpsGain shouldBeGreaterThan result1.dpsGain
        }
    }

    @Nested
    inner class Equality {
        @Test
        fun `results with same itemId should be equal`() {
            // Arrange
            val now = Instant.now()
            val result1 =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = now,
                )
            val result2 =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = now,
                )

            // Assert
            result1 shouldBe result2
        }

        @Test
        fun `results with different itemId should not be equal`() {
            // Arrange
            val now = Instant.now()
            val result1 =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = now,
                )
            val result2 =
                SimulationResult.create(
                    itemId = 12346L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = now,
                )

            // Assert
            result1 shouldNotBe result2
        }
    }
}
