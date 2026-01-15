package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_bonus_ids")
data class LootAwardBonusIdEntity(
    @Id
    val id: Long? = null,
    @Column("loot_award_id")
    val lootAwardId: Long,
    @Column("bonus_id")
    val bonusId: String?,
)
