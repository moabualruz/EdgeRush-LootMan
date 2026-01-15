package com.edgerush.lootman.domain.raidplan.model

/**
 * Value object representing a shape drawn on a raid plan step.
 * Coordinates are normalized to percentage values (0-100).
 */
data class PlanShape(
    val shapeType: ShapeType,
    val x1: Double,
    val y1: Double,
    val x2: Double? = null,
    val y2: Double? = null,
    val radius: Double? = null,
    val color: String? = null,
    val strokeWidth: Int = 2,
) {
    init {
        require(strokeWidth > 0) { "Stroke width must be positive" }
        radius?.let { require(it > 0) { "Radius must be positive" } }
    }

    companion object {
        /**
         * Creates a circle shape.
         */
        fun circle(
            x: Double,
            y: Double,
            radius: Double,
            color: String? = null,
            strokeWidth: Int = 2,
        ): PlanShape = PlanShape(
            shapeType = ShapeType.CIRCLE,
            x1 = x,
            y1 = y,
            radius = radius,
            color = color,
            strokeWidth = strokeWidth,
        )

        /**
         * Creates a line shape.
         */
        fun line(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            color: String? = null,
            strokeWidth: Int = 2,
        ): PlanShape = PlanShape(
            shapeType = ShapeType.LINE,
            x1 = x1,
            y1 = y1,
            x2 = x2,
            y2 = y2,
            color = color,
            strokeWidth = strokeWidth,
        )

        /**
         * Creates an arrow shape.
         */
        fun arrow(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            color: String? = null,
            strokeWidth: Int = 2,
        ): PlanShape = PlanShape(
            shapeType = ShapeType.ARROW,
            x1 = x1,
            y1 = y1,
            x2 = x2,
            y2 = y2,
            color = color,
            strokeWidth = strokeWidth,
        )

        /**
         * Creates a rectangle shape.
         */
        fun rectangle(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
            color: String? = null,
            strokeWidth: Int = 2,
        ): PlanShape = PlanShape(
            shapeType = ShapeType.RECTANGLE,
            x1 = x1,
            y1 = y1,
            x2 = x2,
            y2 = y2,
            color = color,
            strokeWidth = strokeWidth,
        )
    }
}
