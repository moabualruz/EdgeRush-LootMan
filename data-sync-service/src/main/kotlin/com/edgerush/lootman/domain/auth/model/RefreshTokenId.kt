package com.edgerush.lootman.domain.auth.model

/**
 * Value object representing a Refresh Token identifier.
 */
data class RefreshTokenId(val value: Long) {
    init {
        require(value > 0) { "Refresh Token ID must be positive, got $value" }
    }
}
