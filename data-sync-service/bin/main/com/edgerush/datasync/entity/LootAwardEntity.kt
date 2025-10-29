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
    val awardedAt: java.time.OffsetDateTime,
    val rclootcouncilId: String?,
    val icon: String?,
    val slot: String?,
    val quality: String?,
    val responseTypeId: Int?,
    val responseTypeName: String?,
    val responseTypeRgba: String?,
    val responseTypeExcluded: Boolean?,
    val propagatedResponseTypeId: Int?,
    val propagatedResponseTypeName: String?,
    val propagatedResponseTypeRgba: String?,
    val propagatedResponseTypeExcluded: Boolean?,
    val sameResponseAmount: Int?,
    val note: String?,
    val wishValue: Int?,
    val difficulty: String?,
    val discarded: Boolean?,
    val characterId: Long?,
    val awardedByCharacterId: Long?,
    val awardedByName: String?
)
