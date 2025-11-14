package com.edgerush.lootman.domain.loot.model

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

/**
 * Aggregate root representing a loot award.
 *
 * A loot award records when an item was given to a raider, including the FLPS score
 * at the time of award and the ability to revoke the award if needed.
 */
data class LootAward(
    val id: LootAwardId,
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val awardedAt: Instant,
    val flpsScore: FlpsScore,
    val tier: LootTier,
    private val status: LootAwardStatus = LootAwardStatus.ACTIVE,
) {
    fun isActive(): Boolean = status == LootAwardStatus.ACTIVE

    fun revoke(reason: String): LootAward {
        require(status == LootAwardStatus.ACTIVE) {
            "Can only revoke active loot awards"
        }
        return copy(status = LootAwardStatus.REVOKED)
    }

    companion object {
        fun create(
            itemId: ItemId,
            raiderId: RaiderId,
            guildId: GuildId,
            flpsScore: FlpsScore,
            tier: LootTier,
        ): LootAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = itemId,
                raiderId = raiderId,
                guildId = guildId,
                awardedAt = Instant.now(),
                flpsScore = flpsScore,
                tier = tier,
            )
    }
}

enum class LootAwardStatus {
    ACTIVE,
    REVOKED,
}
