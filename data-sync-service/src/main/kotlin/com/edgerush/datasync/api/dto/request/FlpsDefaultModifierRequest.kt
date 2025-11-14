package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.OffsetDateTime

data class CreateFlpsDefaultModifierRequest(
    @field:NotBlank(message = "Category is required")
    @field:Size(max = 255, message = "Category must not exceed 255 characters")
    val category: String? = null,

    @field:NotBlank(message = "Modifier key is required")
    @field:Size(max = 255, message = "Modifier key must not exceed 255 characters")
    val modifierKey: String? = null,

    @field:DecimalMin(value = "0.0", message = "Modifier value must be at least 0.0")
    val modifierValue: BigDecimal? = null,

    val description: String? = null
)

data class UpdateFlpsDefaultModifierRequest(
    @field:NotBlank(message = "Category is required")
    @field:Size(max = 255, message = "Category must not exceed 255 characters")
    val category: String? = null,

    @field:NotBlank(message = "Modifier key is required")
    @field:Size(max = 255, message = "Modifier key must not exceed 255 characters")
    val modifierKey: String? = null,

    @field:DecimalMin(value = "0.0", message = "Modifier value must be at least 0.0")
    val modifierValue: BigDecimal? = null,

    val description: String? = null
)
