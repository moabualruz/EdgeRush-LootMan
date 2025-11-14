package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_wish_data")
data class LootAwardWishDataEntity(
    @Id
    val id: Long? = null,
    val lootAwardId: Long,
    val specName: String?,
    val specIcon: String?,
    val value: Int?,
)
