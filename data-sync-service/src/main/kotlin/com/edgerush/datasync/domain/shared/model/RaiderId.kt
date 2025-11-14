package com.edgerush.datasync.domain.shared.model

/**
 * Value object representing a unique raider identifier.
 */
@JvmInline
value class RaiderId(val value: Long) {
    companion object {
        fun of(value: Long): RaiderId = RaiderId(value)
        
        fun generate(): RaiderId {
            throw UnsupportedOperationException("ID generation should be handled by the database")
        }
    }
}
