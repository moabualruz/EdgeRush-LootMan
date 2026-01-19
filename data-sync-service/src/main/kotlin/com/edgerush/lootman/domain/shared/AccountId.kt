package com.edgerush.lootman.domain.shared

/**
 * Value object representing a user account identifier.
 *
 * AccountId is used to link multiple characters to the same player account.
 * This enables account-level attendance aggregation across all of a
 * player's characters within a guild.
 */
data class AccountId(val value: Long) {
    init {
        require(value > 0) { "Account ID must be positive, got $value" }
    }
}
