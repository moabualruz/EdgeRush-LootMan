package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateLootAwardOldItemRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,
    val itemId: Long? = null,
    val bonusId: String? = null,
)

data class UpdateLootAwardOldItemRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,
    val itemId: Long? = null,
    val bonusId: String? = null,
)
