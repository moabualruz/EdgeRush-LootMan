package com.edgerush.lootman.domain.raidplan.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for PlanMarker value object.
 */
class PlanMarkerTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid marker with all fields`() {
            // Arrange & Act
            val marker =
                PlanMarker(
                    type = MarkerType.SKULL,
                    x = 50.0,
                    y = 75.0,
                    label = "Main Tank",
                    color = "#FF0000",
                )

            // Assert
            marker.type shouldBe MarkerType.SKULL
            marker.x shouldBe 50.0
            marker.y shouldBe 75.0
            marker.label shouldBe "Main Tank"
            marker.color shouldBe "#FF0000"
        }

        @Test
        fun `should create marker with minimal fields`() {
            // Arrange & Act
            val marker =
                PlanMarker(
                    type = MarkerType.CROSS,
                    x = 0.0,
                    y = 0.0,
                )

            // Assert
            marker.type shouldBe MarkerType.CROSS
            marker.x shouldBe 0.0
            marker.y shouldBe 0.0
            marker.label shouldBe null
            marker.color shouldBe null
        }

        @Test
        fun `should create marker with null optional fields`() {
            // Arrange & Act
            val marker =
                PlanMarker(
                    type = MarkerType.MOON,
                    x = 25.5,
                    y = 30.5,
                    label = null,
                    color = null,
                )

            // Assert
            marker.label shouldBe null
            marker.color shouldBe null
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when x is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanMarker(MarkerType.SKULL, -1.0, 50.0)
                }
            exception.message shouldBe "X coordinate cannot be negative"
        }

        @Test
        fun `should throw exception when y is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanMarker(MarkerType.SKULL, 50.0, -1.0)
                }
            exception.message shouldBe "Y coordinate cannot be negative"
        }

        @Test
        fun `should throw exception when x is greater than 100`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanMarker(MarkerType.SKULL, 101.0, 50.0)
                }
            exception.message shouldBe "X coordinate cannot exceed 100"
        }

        @Test
        fun `should throw exception when y is greater than 100`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanMarker(MarkerType.SKULL, 50.0, 101.0)
                }
            exception.message shouldBe "Y coordinate cannot exceed 100"
        }

        @Test
        fun `should allow x and y at boundary values 0 and 100`() {
            // Arrange & Act
            val markerAtOrigin = PlanMarker(MarkerType.SKULL, 0.0, 0.0)
            val markerAtMax = PlanMarker(MarkerType.SKULL, 100.0, 100.0)

            // Assert
            markerAtOrigin.x shouldBe 0.0
            markerAtOrigin.y shouldBe 0.0
            markerAtMax.x shouldBe 100.0
            markerAtMax.y shouldBe 100.0
        }
    }

    @Nested
    inner class MarkerTypeTests {
        @Test
        fun `should have all raid marker types`() {
            // Assert - verify all WoW raid markers exist
            MarkerType.SKULL shouldNotBe null
            MarkerType.CROSS shouldNotBe null
            MarkerType.SQUARE shouldNotBe null
            MarkerType.MOON shouldNotBe null
            MarkerType.TRIANGLE shouldNotBe null
            MarkerType.DIAMOND shouldNotBe null
            MarkerType.CIRCLE shouldNotBe null
            MarkerType.STAR shouldNotBe null
        }

        @Test
        fun `should have role marker types`() {
            // Assert - verify role markers exist
            MarkerType.TANK shouldNotBe null
            MarkerType.HEALER shouldNotBe null
            MarkerType.DPS shouldNotBe null
        }

        @Test
        fun `should have player marker type`() {
            // Assert
            MarkerType.PLAYER shouldNotBe null
        }
    }

    @Nested
    inner class EqualityTests {
        @Test
        fun `should be equal when all fields match`() {
            // Arrange
            val marker1 = PlanMarker(MarkerType.SKULL, 50.0, 50.0, "Tank", "#FF0000")
            val marker2 = PlanMarker(MarkerType.SKULL, 50.0, 50.0, "Tank", "#FF0000")

            // Assert
            marker1 shouldBe marker2
        }

        @Test
        fun `should not be equal when type differs`() {
            // Arrange
            val marker1 = PlanMarker(MarkerType.SKULL, 50.0, 50.0)
            val marker2 = PlanMarker(MarkerType.CROSS, 50.0, 50.0)

            // Assert
            marker1 shouldNotBe marker2
        }

        @Test
        fun `should not be equal when position differs`() {
            // Arrange
            val marker1 = PlanMarker(MarkerType.SKULL, 50.0, 50.0)
            val marker2 = PlanMarker(MarkerType.SKULL, 51.0, 50.0)

            // Assert
            marker1 shouldNotBe marker2
        }
    }

    @Nested
    inner class CopyTests {
        @Test
        fun `should create copy with updated position`() {
            // Arrange
            val original = PlanMarker(MarkerType.SKULL, 10.0, 20.0, "Tank")

            // Act
            val moved = original.copy(x = 30.0, y = 40.0)

            // Assert
            moved.x shouldBe 30.0
            moved.y shouldBe 40.0
            moved.type shouldBe MarkerType.SKULL
            moved.label shouldBe "Tank"
        }

        @Test
        fun `should create copy with updated label`() {
            // Arrange
            val original = PlanMarker(MarkerType.SKULL, 10.0, 20.0, "Tank")

            // Act
            val updated = original.copy(label = "Off Tank")

            // Assert
            updated.label shouldBe "Off Tank"
            updated.x shouldBe 10.0
            updated.y shouldBe 20.0
        }
    }
}
