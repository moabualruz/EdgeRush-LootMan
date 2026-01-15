package com.edgerush.lootman.domain.raidplan.model

/**
 * Entity representing a single step/frame in a raid plan.
 * Each step contains markers and shapes for positioning.
 */
@ConsistentCopyVisibility
data class PlanStep private constructor(
    val order: Int,
    val notes: String?,
    val markers: List<PlanMarker>,
    val shapes: List<PlanShape>,
) {
    init {
        require(order >= 0) { "Step order cannot be negative" }
    }

    /**
     * Adds a marker to this step.
     */
    fun addMarker(marker: PlanMarker): PlanStep =
        copy(
            markers = markers + marker,
        )

    /**
     * Removes a marker at the specified index.
     */
    fun removeMarkerAt(index: Int): PlanStep {
        val newMarkers = markers.toMutableList()
        newMarkers.removeAt(index)
        return copy(markers = newMarkers)
    }

    /**
     * Clears all markers from this step.
     */
    fun clearMarkers(): PlanStep = copy(markers = emptyList())

    /**
     * Adds a shape to this step.
     */
    fun addShape(shape: PlanShape): PlanStep =
        copy(
            shapes = shapes + shape,
        )

    /**
     * Removes a shape at the specified index.
     */
    fun removeShapeAt(index: Int): PlanStep {
        val newShapes = shapes.toMutableList()
        newShapes.removeAt(index)
        return copy(shapes = newShapes)
    }

    /**
     * Clears all shapes from this step.
     */
    fun clearShapes(): PlanStep = copy(shapes = emptyList())

    /**
     * Returns a copy with updated notes.
     */
    fun withNotes(notes: String?): PlanStep = copy(notes = notes)

    /**
     * Returns a copy with updated order.
     */
    fun withOrder(order: Int): PlanStep = copy(order = order)

    /**
     * Returns a copy with the specified markers.
     */
    fun withMarkers(markers: List<PlanMarker>): PlanStep = copy(markers = markers)

    /**
     * Returns a copy with the specified shapes.
     */
    fun withShapes(shapes: List<PlanShape>): PlanStep = copy(shapes = shapes)

    companion object {
        /**
         * Creates a new plan step.
         */
        fun create(
            order: Int,
            notes: String? = null,
            markers: List<PlanMarker> = emptyList(),
            shapes: List<PlanShape> = emptyList(),
        ): PlanStep =
            PlanStep(
                order = order,
                notes = notes,
                markers = markers,
                shapes = shapes,
            )
    }
}
