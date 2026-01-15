package com.edgerush.lootman.domain.raidplan.model

/**
 * Value object representing a marker on a raid plan step.
 * Coordinates are normalized to percentage values (0-100).
 */
data class PlanMarker(
    val type: MarkerType,
    val x: Double,
    val y: Double,
    val label: String? = null,
    val color: String? = null,
) {
    init {
        require(x >= 0) { "X coordinate cannot be negative" }
        require(y >= 0) { "Y coordinate cannot be negative" }
        require(x <= 100) { "X coordinate cannot exceed 100" }
        require(y <= 100) { "Y coordinate cannot exceed 100" }
    }
}
