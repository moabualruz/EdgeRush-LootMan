package com.edgerush.lootman.api.flps

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime

// ============== Default Modifier DTOs ==============

/**
 * Request DTO for creating a default FLPS modifier.
 */
data class CreateFlpsDefaultModifierRequest(
    @field:NotBlank(message = "Category is required")
    val category: String,
    @field:NotBlank(message = "Modifier key is required")
    val modifierKey: String,
    @field:NotNull(message = "Modifier value is required")
    val modifierValue: BigDecimal,
    val description: String? = null,
)

/**
 * Request DTO for updating a default FLPS modifier.
 */
data class UpdateFlpsDefaultModifierRequest(
    val category: String? = null,
    val modifierKey: String? = null,
    val modifierValue: BigDecimal? = null,
    val description: String? = null,
)

/**
 * Response DTO for a default FLPS modifier.
 */
data class FlpsDefaultModifierResponse(
    val id: Long,
    val category: String,
    val modifierKey: String,
    val modifierValue: BigDecimal,
    val description: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: FlpsDefaultModifierEntity): FlpsDefaultModifierResponse =
            FlpsDefaultModifierResponse(
                id = entity.id ?: 0L,
                category = entity.category,
                modifierKey = entity.modifierKey,
                modifierValue = entity.modifierValue,
                description = entity.description,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
    }
}

// ============== Guild Modifier DTOs ==============

/**
 * Request DTO for creating a guild-specific FLPS modifier.
 */
data class CreateFlpsGuildModifierRequest(
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    @field:NotBlank(message = "Category is required")
    val category: String,
    @field:NotBlank(message = "Modifier key is required")
    val modifierKey: String,
    @field:NotNull(message = "Modifier value is required")
    val modifierValue: BigDecimal,
    val description: String? = null,
)

/**
 * Request DTO for updating a guild-specific FLPS modifier.
 */
data class UpdateFlpsGuildModifierRequest(
    val category: String? = null,
    val modifierKey: String? = null,
    val modifierValue: BigDecimal? = null,
    val description: String? = null,
)

/**
 * Response DTO for a guild-specific FLPS modifier.
 */
data class FlpsGuildModifierResponse(
    val id: Long,
    val guildId: String,
    val category: String,
    val modifierKey: String,
    val modifierValue: BigDecimal,
    val description: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(entity: FlpsGuildModifierEntity): FlpsGuildModifierResponse =
            FlpsGuildModifierResponse(
                id = entity.id ?: 0L,
                guildId = entity.guildId,
                category = entity.category,
                modifierKey = entity.modifierKey,
                modifierValue = entity.modifierValue,
                description = entity.description,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
    }
}
