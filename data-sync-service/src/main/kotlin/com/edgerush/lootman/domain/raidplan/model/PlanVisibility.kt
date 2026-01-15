package com.edgerush.lootman.domain.raidplan.model

/**
 * Visibility levels for raid plans.
 */
enum class PlanVisibility {
    /** Only visible to the creator */
    PRIVATE,

    /** Visible to all guild members */
    GUILD,

    /** Visible to anyone with the link */
    PUBLIC,
}
