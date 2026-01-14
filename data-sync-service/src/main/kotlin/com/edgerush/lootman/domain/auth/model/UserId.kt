package com.edgerush.lootman.domain.auth.model

/**
 * Value object representing a User identifier.
 */
data class UserId(val value: Long) {
    init {
        require(value > 0) { "User ID must be positive, got $value" }
    }
}
