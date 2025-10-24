package com.edgerush.datasync.api.wowaudit

data class LootResponse(
    val loot: List<LootEntryDto>
)

data class LootEntryDto(
    val character: String,
    val item: LootItemDto,
    val tier: String,
    val awardedAt: String?
)

data class LootItemDto(
    val id: Long,
    val name: String
)
