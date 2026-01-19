package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("loot_awards")
data class LootAwardEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("item_id")
    val itemId: Long,
    @Column("item_name")
    val itemName: String,
    @Column("tier")
    val tier: String,
    @Column("flps")
    val flps: Double,
    @Column("rdf")
    val rdf: Double,
    @Column("awarded_at")
    val awardedAt: OffsetDateTime,
    @Column("rclootcouncil_id")
    val rclootcouncilId: String?,
    @Column("icon")
    val icon: String?,
    @Column("slot")
    val slot: String?,
    @Column("quality")
    val quality: String?,
    @Column("response_type_id")
    val responseTypeId: Int?,
    @Column("response_type_name")
    val responseTypeName: String?,
    @Column("response_type_rgba")
    val responseTypeRgba: String?,
    @Column("response_type_excluded")
    val responseTypeExcluded: Boolean?,
    @Column("propagated_response_type_id")
    val propagatedResponseTypeId: Int?,
    @Column("propagated_response_type_name")
    val propagatedResponseTypeName: String?,
    @Column("propagated_response_type_rgba")
    val propagatedResponseTypeRgba: String?,
    @Column("propagated_response_type_excluded")
    val propagatedResponseTypeExcluded: Boolean?,
    @Column("same_response_amount")
    val sameResponseAmount: Int?,
    @Column("note")
    val note: String?,
    @Column("wish_value")
    val wishValue: Int?,
    @Column("difficulty")
    val difficulty: String?,
    @Column("discarded")
    val discarded: Boolean?,
    @Column("character_id")
    val characterId: Long?,
    @Column("awarded_by_character_id")
    val awardedByCharacterId: Long?,
    @Column("awarded_by_name")
    val awardedByName: String?,
)
