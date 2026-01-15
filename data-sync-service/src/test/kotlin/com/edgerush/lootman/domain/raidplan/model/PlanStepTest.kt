package com.edgerush.lootman.domain.raidplan.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for PlanStep entity.
 */
class PlanStepTest : UnitTest() {

    @Nested
    inner class CreationTests {

        @Test
        fun `should create valid step with order and notes`() {
            // Arrange & Act
            val step = PlanStep.create(order = 0, notes = "Initial positions")

            // Assert
            step.order shouldBe 0
            step.notes shouldBe "Initial positions"
            step.markers.shouldBeEmpty()
            step.shapes.shouldBeEmpty()
        }

        @Test
        fun `should create step with null notes`() {
            // Arrange & Act
            val step = PlanStep.create(order = 1, notes = null)

            // Assert
            step.order shouldBe 1
            step.notes shouldBe null
        }

        @Test
        fun `should create step with empty markers and shapes`() {
            // Arrange & Act
            val step = PlanStep.create(order = 0)

            // Assert
            step.markers.shouldBeEmpty()
            step.shapes.shouldBeEmpty()
        }
    }

    @Nested
    inner class ValidationTests {

        @Test
        fun `should throw exception when order is negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                PlanStep.create(order = -1)
            }
            exception.message shouldBe "Step order cannot be negative"
        }
    }

    @Nested
    inner class MarkerManagementTests {

        @Test
        fun `should add marker to step`() {
            // Arrange
            val step = PlanStep.create(order = 0)
            val marker = PlanMarker(
                type = MarkerType.SKULL,
                x = 50.0,
                y = 50.0,
                label = "Tank position",
                color = null,
            )

            // Act
            val updatedStep = step.addMarker(marker)

            // Assert
            updatedStep.markers shouldHaveSize 1
            updatedStep.markers[0].type shouldBe MarkerType.SKULL
            updatedStep.markers[0].x shouldBe 50.0
            updatedStep.markers[0].y shouldBe 50.0
            updatedStep.markers[0].label shouldBe "Tank position"
        }

        @Test
        fun `should add multiple markers to step`() {
            // Arrange
            val step = PlanStep.create(order = 0)

            // Act
            val updatedStep = step
                .addMarker(PlanMarker(MarkerType.SKULL, 10.0, 10.0))
                .addMarker(PlanMarker(MarkerType.CROSS, 20.0, 20.0))
                .addMarker(PlanMarker(MarkerType.SQUARE, 30.0, 30.0))

            // Assert
            updatedStep.markers shouldHaveSize 3
        }

        @Test
        fun `should remove marker at index`() {
            // Arrange
            val step = PlanStep.create(order = 0)
                .addMarker(PlanMarker(MarkerType.SKULL, 10.0, 10.0))
                .addMarker(PlanMarker(MarkerType.CROSS, 20.0, 20.0))
                .addMarker(PlanMarker(MarkerType.SQUARE, 30.0, 30.0))

            // Act
            val updatedStep = step.removeMarkerAt(1)

            // Assert
            updatedStep.markers shouldHaveSize 2
            updatedStep.markers[0].type shouldBe MarkerType.SKULL
            updatedStep.markers[1].type shouldBe MarkerType.SQUARE
        }

        @Test
        fun `should throw exception when removing marker at invalid index`() {
            // Arrange
            val step = PlanStep.create(order = 0)
                .addMarker(PlanMarker(MarkerType.SKULL, 10.0, 10.0))

            // Act & Assert
            val exception = shouldThrow<IndexOutOfBoundsException> {
                step.removeMarkerAt(5)
            }
        }

        @Test
        fun `should clear all markers`() {
            // Arrange
            val step = PlanStep.create(order = 0)
                .addMarker(PlanMarker(MarkerType.SKULL, 10.0, 10.0))
                .addMarker(PlanMarker(MarkerType.CROSS, 20.0, 20.0))

            // Act
            val updatedStep = step.clearMarkers()

            // Assert
            updatedStep.markers.shouldBeEmpty()
        }
    }

    @Nested
    inner class ShapeManagementTests {

        @Test
        fun `should add shape to step`() {
            // Arrange
            val step = PlanStep.create(order = 0)
            val shape = PlanShape.circle(
                x = 50.0,
                y = 50.0,
                radius = 20.0,
                color = "#FF0000",
            )

            // Act
            val updatedStep = step.addShape(shape)

            // Assert
            updatedStep.shapes shouldHaveSize 1
            updatedStep.shapes[0].shapeType shouldBe ShapeType.CIRCLE
            updatedStep.shapes[0].x1 shouldBe 50.0
            updatedStep.shapes[0].y1 shouldBe 50.0
            updatedStep.shapes[0].radius shouldBe 20.0
        }

        @Test
        fun `should add line shape to step`() {
            // Arrange
            val step = PlanStep.create(order = 0)
            val line = PlanShape.line(
                x1 = 10.0,
                y1 = 10.0,
                x2 = 90.0,
                y2 = 90.0,
                color = "#00FF00",
                strokeWidth = 3,
            )

            // Act
            val updatedStep = step.addShape(line)

            // Assert
            updatedStep.shapes shouldHaveSize 1
            updatedStep.shapes[0].shapeType shouldBe ShapeType.LINE
            updatedStep.shapes[0].x1 shouldBe 10.0
            updatedStep.shapes[0].y1 shouldBe 10.0
            updatedStep.shapes[0].x2 shouldBe 90.0
            updatedStep.shapes[0].y2 shouldBe 90.0
        }

        @Test
        fun `should add arrow shape to step`() {
            // Arrange
            val step = PlanStep.create(order = 0)
            val arrow = PlanShape.arrow(
                x1 = 10.0,
                y1 = 10.0,
                x2 = 50.0,
                y2 = 50.0,
                color = "#0000FF",
            )

            // Act
            val updatedStep = step.addShape(arrow)

            // Assert
            updatedStep.shapes shouldHaveSize 1
            updatedStep.shapes[0].shapeType shouldBe ShapeType.ARROW
        }

        @Test
        fun `should remove shape at index`() {
            // Arrange
            val step = PlanStep.create(order = 0)
                .addShape(PlanShape.circle(10.0, 10.0, 5.0))
                .addShape(PlanShape.line(0.0, 0.0, 100.0, 100.0))

            // Act
            val updatedStep = step.removeShapeAt(0)

            // Assert
            updatedStep.shapes shouldHaveSize 1
            updatedStep.shapes[0].shapeType shouldBe ShapeType.LINE
        }

        @Test
        fun `should clear all shapes`() {
            // Arrange
            val step = PlanStep.create(order = 0)
                .addShape(PlanShape.circle(10.0, 10.0, 5.0))
                .addShape(PlanShape.line(0.0, 0.0, 100.0, 100.0))

            // Act
            val updatedStep = step.clearShapes()

            // Assert
            updatedStep.shapes.shouldBeEmpty()
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should update notes`() {
            // Arrange
            val step = PlanStep.create(order = 0, notes = "Original")

            // Act
            val updatedStep = step.withNotes("Updated notes")

            // Assert
            updatedStep.notes shouldBe "Updated notes"
        }

        @Test
        fun `should update order`() {
            // Arrange
            val step = PlanStep.create(order = 0)

            // Act
            val updatedStep = step.withOrder(5)

            // Assert
            updatedStep.order shouldBe 5
        }
    }
}
