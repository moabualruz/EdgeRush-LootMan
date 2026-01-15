package com.edgerush.lootman.domain.raidplan.model

/**
 * Types of markers that can be placed on a raid plan.
 * Includes WoW's standard raid markers and role-based markers.
 */
enum class MarkerType {
    // Standard WoW raid markers
    SKULL,
    CROSS,      // X marker
    SQUARE,
    MOON,
    TRIANGLE,
    DIAMOND,
    CIRCLE,
    STAR,

    // Role-based markers
    TANK,
    HEALER,
    DPS,

    // Generic player marker
    PLAYER,
}
