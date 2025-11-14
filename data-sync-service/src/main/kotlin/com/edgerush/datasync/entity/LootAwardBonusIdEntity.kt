package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_bonus_ids")
data class LootAwardBonusIdEntity(
    @Id
    val id: Long? = null,
    val lootAwardId: Long,
    val bonusId: String?,
)
