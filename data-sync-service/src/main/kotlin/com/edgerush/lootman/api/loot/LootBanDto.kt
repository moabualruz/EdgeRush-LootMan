package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootBanEntity
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * Request DTO for creating a loot ban (entity-level CRUD).
 */
data class CreateLootBanEntityRequest(
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    @field:NotBlank(message = "Reason is required")
    val reason: String,
    @field:NotBlank(message = "Banned by is required")
    val bannedBy: String,
    val expiresAt: LocalDateTime? = null,
)

/**
 * Request DTO for updating a loot ban (entity-level CRUD).
 */
data class UpdateLootBanEntityRequest(
    val reason: String? = null,
    val expiresAt: LocalDateTime? = null,
    val isActive: Boolean? = null,
)

/**
 * Response DTO for a loot ban (entity-level CRUD).
 */
data class LootBanResponse(
    val id: Long,
    val guildId: String,
    val characterName: String,
    val reason: String,
    val bannedBy: String,
    val bannedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
) {
    companion object {
        fun from(entity: LootBanEntity): LootBanResponse =
            LootBanResponse(
                id = entity.id ?: 0L,
                guildId = entity.guildId,
                characterName = entity.characterName,
                reason = entity.reason,
                bannedBy = entity.bannedBy,
                bannedAt = entity.bannedAt,
                expiresAt = entity.expiresAt,
                isActive = entity.isActive,
            )
    }
}

/**
 * Response DTO for checking if a character is banned.
 */
data class BannedResponse(
    val banned: Boolean,
)

/**
 * Response DTO for checking if a loot ban exists.
 */
data class LootBanExistsResponse(
    val exists: Boolean,
)

/**
 * Response DTO for loot ban count.
 */
data class LootBanCountResponse(
    val count: Long,
)
