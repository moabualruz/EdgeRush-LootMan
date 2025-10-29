package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Table("flps_default_modifiers")
data class FlpsDefaultModifierEntity(
    @Id
    val id: Long? = null,
    val category: String,
    val modifierKey: String,
    val modifierValue: BigDecimal,
    val description: String?,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)