package com.edgerush.datasync.domain.raids.model

import java.util.UUID

/**
 * Value object representing a unique raid identifier.
 */
@JvmInline
value class RaidId(val value: Long) {
    companion object {
        fun generate(): RaidId = RaidId(System.currentTimeMillis())
        
        fun of(value: Long): RaidId = RaidId(value)
    }
}
