package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

/**
 * Request DTO for creating a loot award entity.
 */
data class CreateLootAwardEntityRequest(
    @field:NotNull(message = "Raider ID is required")
    @field:Positive(message = "Raider ID must be positive")
    val raiderId: Long,
    @field:NotNull(message = "Item ID is required")
    @field:Positive(message = "Item ID must be positive")
    val itemId: Long,
    @field:NotBlank(message = "Item name is required")
    val itemName: String,
    @field:NotBlank(message = "Tier is required")
    val tier: String,
    val flps: Double = 0.0,
    val rdf: Double = 0.0,
    val rclootcouncilId: String? = null,
    val icon: String? = null,
    val slot: String? = null,
    val quality: String? = null,
    val responseTypeId: Int? = null,
    val responseTypeName: String? = null,
    val note: String? = null,
    val wishValue: Int? = null,
    val difficulty: String? = null,
    val discarded: Boolean? = false,
    val characterId: Long? = null,
    val awardedByCharacterId: Long? = null,
    val awardedByName: String? = null,
)

/**
 * Request DTO for updating a loot award entity.
 */
data class UpdateLootAwardEntityRequest(
    val note: String? = null,
    val discarded: Boolean? = null,
    val wishValue: Int? = null,
)

/**
 * Response DTO for a loot award entity.
 */
data class LootAwardEntityResponse(
    val id: Long,
    val raiderId: Long,
    val itemId: Long,
    val itemName: String,
    val tier: String,
    val flps: Double,
    val rdf: Double,
    val awardedAt: OffsetDateTime,
    val rclootcouncilId: String?,
    val icon: String?,
    val slot: String?,
    val quality: String?,
    val responseTypeId: Int?,
    val responseTypeName: String?,
    val note: String?,
    val wishValue: Int?,
    val difficulty: String?,
    val discarded: Boolean?,
    val characterId: Long?,
    val awardedByCharacterId: Long?,
    val awardedByName: String?,
) {
    companion object {
        fun from(entity: LootAwardEntity): LootAwardEntityResponse =
            LootAwardEntityResponse(
                id = entity.id ?: 0L,
                raiderId = entity.raiderId,
                itemId = entity.itemId,
                itemName = entity.itemName,
                tier = entity.tier,
                flps = entity.flps,
                rdf = entity.rdf,
                awardedAt = entity.awardedAt,
                rclootcouncilId = entity.rclootcouncilId,
                icon = entity.icon,
                slot = entity.slot,
                quality = entity.quality,
                responseTypeId = entity.responseTypeId,
                responseTypeName = entity.responseTypeName,
                note = entity.note,
                wishValue = entity.wishValue,
                difficulty = entity.difficulty,
                discarded = entity.discarded,
                characterId = entity.characterId,
                awardedByCharacterId = entity.awardedByCharacterId,
                awardedByName = entity.awardedByName,
            )
    }
}

/**
 * Response DTO for checking if a loot award exists.
 */
data class LootAwardExistsResponse(
    val exists: Boolean,
)

/**
 * Response DTO for loot award count.
 */
data class LootAwardCountResponse(
    val count: Long,
)
