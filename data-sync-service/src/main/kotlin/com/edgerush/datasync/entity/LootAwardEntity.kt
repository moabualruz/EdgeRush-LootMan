package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("loot_awards")
data class LootAwardEntity(
    @Id
    val id: Long? = null,
    val raiderId: Long,
    val itemId: Long,
    val itemName: String,
    val tier: String,
    val flps: Double,
    val rdf: Double,
    val awardedAt: java.time.OffsetDateTime
)
