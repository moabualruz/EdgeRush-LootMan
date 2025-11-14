package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateLootAwardWishDataRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,
    val specName: String? = null,
    val specIcon: String? = null,
    val value: Int? = null,
)

data class UpdateLootAwardWishDataRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,
    val specName: String? = null,
    val specIcon: String? = null,
    val value: Int? = null,
)
