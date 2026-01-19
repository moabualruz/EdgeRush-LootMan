package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("flps_guild_modifiers")
data class FlpsGuildModifierEntity(
    @Id
    val id: Long? = null,
    @Column("guild_id")
    val guildId: String,
    @Column("category")
    val category: String,
    @Column("modifier_key")
    val modifierKey: String,
    @Column("modifier_value")
    val modifierValue: BigDecimal,
    @Column("description")
    val description: String?,
    @Column("created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column("updated_at")
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
