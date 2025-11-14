package com.edgerush.lootman.domain.shared

/**
 * Value object representing an Item identifier.
 */
data class ItemId(val value: Long) {
    init {
        require(value > 0) { "Item ID must be positive" }
    }
}
