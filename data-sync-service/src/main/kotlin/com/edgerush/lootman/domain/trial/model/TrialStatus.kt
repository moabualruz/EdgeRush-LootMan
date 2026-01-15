package com.edgerush.lootman.domain.trial.model

/**
 * Enumeration of trial statuses.
 */
enum class TrialStatus(val isTerminal: Boolean) {
    /** Trial is currently active and being evaluated */
    ACTIVE(false),

    /** Trial was extended for additional evaluation */
    EXTENDED(false),

    /** Trial ended with raider being promoted to full member */
    PROMOTED(true),

    /** Trial ended without promotion */
    ENDED(true),
}
