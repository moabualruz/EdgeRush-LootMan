package com.edgerush.lootman.api.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * Request DTO for awarding loot.
 */
data class AwardLootRequest(
    @field:Min(value = 1, message = "Item ID must be positive")
    val itemId: Long,

    @field:NotBlank(message = "Raider ID is required")
    val raiderId: String,

    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,

    @field:DecimalMin(value = "0.0", message = "FLPS score must be non-negative")
    @field:DecimalMax(value = "1.0", message = "FLPS score cannot exceed 1.0")
    val flpsScore: Double,

    @field:NotBlank(message = "Tier is required")
    val tier: String,
)

/**
 * Request DTO for creating a loot ban.
 */
data class CreateLootBanRequest(
    @field:NotBlank(message = "Raider ID is required")
    val raiderId: String,

    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,

    @field:NotBlank(message = "Reason is required")
    @field:Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    val reason: String,

    val expiresAt: Instant?,
)

/**
 * Response DTO for loot history.
 */
data class LootHistoryResponse(
    val awards: List<LootAwardDto>,
) {
    companion object {
        fun from(awards: List<LootAward>): LootHistoryResponse {
            return LootHistoryResponse(
                awards = awards.map { LootAwardDto.from(it) },
            )
        }
    }
}

/**
 * DTO for a single loot award.
 */
data class LootAwardDto(
    val id: String,
    val itemId: Long,
    val raiderId: String,
    val guildId: String,
    val awardedAt: Instant,
    val flpsScore: Double,
    val tier: String,
    val isActive: Boolean,
) {
    companion object {
        fun from(award: LootAward): LootAwardDto {
            return LootAwardDto(
                id = award.id.value,
                itemId = award.itemId.value,
                raiderId = award.raiderId.value.toString(),
                guildId = award.guildId.value,
                awardedAt = award.awardedAt,
                flpsScore = award.flpsScore.value,
                tier = award.tier.name,
                isActive = award.isActive(),
            )
        }
    }
}

/**
 * Response DTO for loot bans.
 */
data class LootBansResponse(
    val bans: List<LootBanDto>,
) {
    companion object {
        fun from(bans: List<LootBan>): LootBansResponse {
            return LootBansResponse(
                bans = bans.map { LootBanDto.from(it) },
            )
        }
    }
}

/**
 * DTO for a single loot ban.
 */
data class LootBanDto(
    val id: String,
    val raiderId: String,
    val guildId: String,
    val reason: String,
    val bannedAt: Instant,
    val expiresAt: Instant?,
    val isActive: Boolean,
) {
    companion object {
        fun from(ban: LootBan): LootBanDto {
            return LootBanDto(
                id = ban.id.value,
                raiderId = ban.raiderId.value.toString(),
                guildId = ban.guildId.value,
                reason = ban.reason,
                bannedAt = ban.bannedAt,
                expiresAt = ban.expiresAt,
                isActive = ban.isActive(),
            )
        }
    }
}

/**
 * Request DTO for updating a loot ban.
 */
data class UpdateLootBanRequest(
    val reason: String? = null,
    val expiresAt: Instant? = null
)

/**
 * Response DTO for a list of loot awards with metadata.
 */
data class LootAwardsListResponse(
    val awards: List<LootAwardDto>,
    val totalCount: Int,
    val activeCount: Int
) {
    companion object {
        fun from(awards: List<LootAward>): LootAwardsListResponse {
            val awardDtos = awards.map { LootAwardDto.from(it) }
            return LootAwardsListResponse(
                awards = awardDtos,
                totalCount = awardDtos.size,
                activeCount = awardDtos.count { it.isActive }
            )
        }
    }
}

/**
 * Response DTO for a list of loot bans with metadata.
 */
data class LootBansListResponse(
    val bans: List<LootBanDto>,
    val totalCount: Int,
    val activeCount: Int
) {
    companion object {
        fun from(bans: List<LootBan>): LootBansListResponse {
            val banDtos = bans.map { LootBanDto.from(it) }
            return LootBansListResponse(
                bans = banDtos,
                totalCount = banDtos.size,
                activeCount = banDtos.count { it.isActive }
            )
        }
    }
}
