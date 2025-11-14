package com.edgerush.lootman.api.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import java.time.Instant

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
                raiderId = award.raiderId.value,
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
                raiderId = ban.raiderId.value,
                guildId = ban.guildId.value,
                reason = ban.reason,
                bannedAt = ban.bannedAt,
                expiresAt = ban.expiresAt,
                isActive = ban.isActive(),
            )
        }
    }
}

