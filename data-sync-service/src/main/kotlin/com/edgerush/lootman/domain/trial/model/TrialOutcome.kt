package com.edgerush.lootman.domain.trial.model

/**
 * Enumeration of trial outcomes.
 */
enum class TrialOutcome(val isSuccessful: Boolean) {
    /** Raider successfully completed trial and was promoted */
    PROMOTED(true),

    /** Raider failed to meet trial requirements */
    FAILED(false),

    /** Raider voluntarily withdrew from trial */
    WITHDREW(false),

    /** Raider was removed by guild officers */
    REMOVED(false)
}
