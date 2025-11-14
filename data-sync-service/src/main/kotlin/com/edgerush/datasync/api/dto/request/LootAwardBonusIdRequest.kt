package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateLootAwardBonusIdRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,

    val bonusId: String? = null
)

data class UpdateLootAwardBonusIdRequest(
    @field:Min(value = 0, message = "Loot award id must be positive")
    val lootAwardId: Long? = null,

    val bonusId: String? = null
)
