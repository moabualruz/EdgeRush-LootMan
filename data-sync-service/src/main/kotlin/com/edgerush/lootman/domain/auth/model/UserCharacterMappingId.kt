package com.edgerush.lootman.domain.auth.model

/**
 * Value object representing a User-Character Mapping identifier.
 */
data class UserCharacterMappingId(val value: Long) {
    init {
        require(value > 0) { "User Character Mapping ID must be positive, got $value" }
    }
}
