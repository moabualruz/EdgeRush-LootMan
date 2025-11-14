package com.edgerush.lootman.domain.loot.model

import java.util.UUID

/**
 * Value object representing a LootAward identifier.
 */
data class LootAwardId(val value: String) {
    init {
        require(value.isNotBlank()) { "LootAward ID cannot be blank" }
    }

    companion object {
        fun generate(): LootAwardId = LootAwardId(UUID.randomUUID().toString())
    }
}
