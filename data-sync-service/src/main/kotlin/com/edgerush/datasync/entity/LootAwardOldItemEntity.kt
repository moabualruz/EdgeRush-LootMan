package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_old_items")
data class LootAwardOldItemEntity(
    @Id
    val id: Long? = null,
    val lootAwardId: Long,
    val itemId: Long?,
    val bonusId: String?,
)
