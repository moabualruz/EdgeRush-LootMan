package com.edgerush.datasync.api.dto.response

import java.math.BigDecimal
import java.time.OffsetDateTime

data class FlpsDefaultModifierResponse(
    val id: Long?,
    val category: String,
    val modifierKey: String,
    val modifierValue: BigDecimal,
    val description: String?,
    val createdAt: OffsetDateTime,
)
