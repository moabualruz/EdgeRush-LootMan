package com.edgerush.lootman.domain.raidplan.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for PlanShape value object.
 */
class PlanShapeTest : UnitTest() {
    @Nested
    inner class CircleCreationTests {
        @Test
        fun `should create valid circle shape`() {
            // Arrange & Act
            val circle =
                PlanShape.circle(
                    x = 50.0,
                    y = 50.0,
                    radius = 15.0,
                    color = "#FF0000",
                )

            // Assert
            circle.shapeType shouldBe ShapeType.CIRCLE
            circle.x1 shouldBe 50.0
            circle.y1 shouldBe 50.0
            circle.radius shouldBe 15.0
            circle.color shouldBe "#FF0000"
            circle.x2 shouldBe null
            circle.y2 shouldBe null
        }

        @Test
        fun `should create circle with default color`() {
            // Arrange & Act
            val circle = PlanShape.circle(50.0, 50.0, 10.0)

            // Assert
            circle.color shouldBe null
        }

        @Test
        fun `should create circle with default stroke width`() {
            // Arrange & Act
            val circle = PlanShape.circle(50.0, 50.0, 10.0)

            // Assert
            circle.strokeWidth shouldBe 2
        }

        @Test
        fun `should throw exception when radius is not positive`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanShape.circle(50.0, 50.0, 0.0)
                }
            exception.message shouldBe "Radius must be positive"
        }

        @Test
        fun `should throw exception when radius is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanShape.circle(50.0, 50.0, -5.0)
                }
            exception.message shouldBe "Radius must be positive"
        }
    }

    @Nested
    inner class LineCreationTests {
        @Test
        fun `should create valid line shape`() {
            // Arrange & Act
            val line =
                PlanShape.line(
                    x1 = 10.0,
                    y1 = 20.0,
                    x2 = 80.0,
                    y2 = 70.0,
                    color = "#00FF00",
                    strokeWidth = 3,
                )

            // Assert
            line.shapeType shouldBe ShapeType.LINE
            line.x1 shouldBe 10.0
            line.y1 shouldBe 20.0
            line.x2 shouldBe 80.0
            line.y2 shouldBe 70.0
            line.color shouldBe "#00FF00"
            line.strokeWidth shouldBe 3
            line.radius shouldBe null
        }

        @Test
        fun `should create line with default stroke width`() {
            // Arrange & Act
            val line = PlanShape.line(0.0, 0.0, 100.0, 100.0)

            // Assert
            line.strokeWidth shouldBe 2
        }
    }

    @Nested
    inner class ArrowCreationTests {
        @Test
        fun `should create valid arrow shape`() {
            // Arrange & Act
            val arrow =
                PlanShape.arrow(
                    x1 = 10.0,
                    y1 = 10.0,
                    x2 = 50.0,
                    y2 = 50.0,
                    color = "#0000FF",
                )

            // Assert
            arrow.shapeType shouldBe ShapeType.ARROW
            arrow.x1 shouldBe 10.0
            arrow.y1 shouldBe 10.0
            arrow.x2 shouldBe 50.0
            arrow.y2 shouldBe 50.0
            arrow.color shouldBe "#0000FF"
        }

        @Test
        fun `should create arrow with custom stroke width`() {
            // Arrange & Act
            val arrow = PlanShape.arrow(10.0, 10.0, 50.0, 50.0, strokeWidth = 5)

            // Assert
            arrow.strokeWidth shouldBe 5
        }
    }

    @Nested
    inner class RectangleCreationTests {
        @Test
        fun `should create valid rectangle shape`() {
            // Arrange & Act
            val rect =
                PlanShape.rectangle(
                    x1 = 20.0,
                    y1 = 20.0,
                    x2 = 80.0,
                    y2 = 60.0,
                    color = "#FFFF00",
                )

            // Assert
            rect.shapeType shouldBe ShapeType.RECTANGLE
            rect.x1 shouldBe 20.0
            rect.y1 shouldBe 20.0
            rect.x2 shouldBe 80.0
            rect.y2 shouldBe 60.0
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when stroke width is not positive`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanShape.line(0.0, 0.0, 100.0, 100.0, strokeWidth = 0)
                }
            exception.message shouldBe "Stroke width must be positive"
        }

        @Test
        fun `should throw exception when stroke width is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PlanShape.circle(50.0, 50.0, 10.0, strokeWidth = -1)
                }
            exception.message shouldBe "Stroke width must be positive"
        }
    }

    @Nested
    inner class ShapeTypeTests {
        @Test
        fun `should have all expected shape types`() {
            // Assert
            ShapeType.CIRCLE shouldNotBe null
            ShapeType.LINE shouldNotBe null
            ShapeType.ARROW shouldNotBe null
            ShapeType.RECTANGLE shouldNotBe null
        }
    }

    @Nested
    inner class EqualityTests {
        @Test
        fun `circles should be equal when all fields match`() {
            // Arrange
            val circle1 = PlanShape.circle(50.0, 50.0, 10.0, "#FF0000")
            val circle2 = PlanShape.circle(50.0, 50.0, 10.0, "#FF0000")

            // Assert
            circle1 shouldBe circle2
        }

        @Test
        fun `lines should be equal when all fields match`() {
            // Arrange
            val line1 = PlanShape.line(0.0, 0.0, 100.0, 100.0, "#00FF00", 2)
            val line2 = PlanShape.line(0.0, 0.0, 100.0, 100.0, "#00FF00", 2)

            // Assert
            line1 shouldBe line2
        }

        @Test
        fun `should not be equal when shape type differs`() {
            // Arrange
            val circle = PlanShape.circle(50.0, 50.0, 10.0)
            val line = PlanShape.line(50.0, 50.0, 60.0, 60.0)

            // Assert
            circle shouldNotBe line
        }
    }
}
