package com.edgerush.lootman.domain.trial.model

import java.util.UUID

/**
 * Value object representing a Trial identifier.
 */
data class TrialId(val value: String) {
    init {
        require(value.isNotBlank()) { "Trial ID cannot be blank" }
    }

    companion object {
        fun generate(): TrialId = TrialId(UUID.randomUUID().toString())
    }
}
