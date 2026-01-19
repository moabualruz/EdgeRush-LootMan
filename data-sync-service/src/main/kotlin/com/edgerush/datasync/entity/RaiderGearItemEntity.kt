package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_gear_items")
data class RaiderGearItemEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("gear_set")
    val gearSet: String,
    @Column("slot")
    val slot: String,
    @Column("item_id")
    val itemId: Long?,
    @Column("item_level")
    val itemLevel: Int?,
    @Column("quality")
    val quality: Int?,
    @Column("enchant")
    val enchant: String?,
    @Column("enchant_quality")
    val enchantQuality: Int?,
    @Column("upgrade_level")
    val upgradeLevel: Int?,
    @Column("sockets")
    val sockets: Int?,
    @Column("name")
    val name: String?,
)
