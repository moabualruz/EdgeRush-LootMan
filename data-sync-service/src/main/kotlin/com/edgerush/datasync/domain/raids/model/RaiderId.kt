package com.edgerush.datasync.domain.raids.model

/**
 * Value object representing a unique raider identifier.
 */
@JvmInline
value class RaiderId(val value: Long) {
    companion object {
        fun of(value: Long): RaiderId = RaiderId(value)
    }
}
