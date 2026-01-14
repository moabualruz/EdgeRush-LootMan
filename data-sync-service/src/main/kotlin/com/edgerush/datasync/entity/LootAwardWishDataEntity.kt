package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("loot_award_wish_data")
data class LootAwardWishDataEntity(
    @Id
    val id: Long? = null,
    @Column("loot_award_id")
    val lootAwardId: Long,
    @Column("spec_name")
    val specName: String?,
    @Column("spec_icon")
    val specIcon: String?,
    val value: Int?
)
