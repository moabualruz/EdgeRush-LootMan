package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_old_items")
data class LootAwardOldItemEntity(
    @Id
    val id: Long? = null,
    @Column("loot_award_id")
    val lootAwardId: Long,
    @Column("item_id")
    val itemId: Long?,
    @Column("bonus_id")
    val bonusId: String?
)
