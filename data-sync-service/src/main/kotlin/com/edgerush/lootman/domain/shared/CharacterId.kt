package com.edgerush.lootman.domain.shared

/**
 * Value object representing a unique Character identifier.
 *
 * CharacterId is the authoritative identity for a WoW character,
 * independent of guild membership (RaiderId). Performance data,
 * attendance, and other tracking data should be linked to CharacterId
 * so that data persists regardless of guild membership status.
 */
data class CharacterId(val value: Long) {
    init {
        require(value > 0) { "Character ID must be positive, got $value" }
    }
}
