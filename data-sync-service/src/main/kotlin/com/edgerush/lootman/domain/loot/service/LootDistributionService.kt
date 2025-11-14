package com.edgerush.lootman.domain.loot.service

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

class LootDistributionService {
    fun isEligibleForLoot(
        raiderId: RaiderId,
        activeBans: List<LootBan>,
        now: Instant = Instant.now(),
    ): Boolean {
        return activeBans.none { it.isActive(now) }
    }

    fun shouldApplyRecencyDecay(
        recentAwards: List<LootAward>,
        decayThresholdDays: Int = 14,
        now: Instant = Instant.now(),
    ): Boolean {
        val thresholdTime = now.minusSeconds(decayThresholdDays.toLong() * 24 * 60 * 60)
        return recentAwards.any { it.awardedAt.isAfter(thresholdTime) }
    }

    fun calculateEffectiveScore(
        baseScore: FlpsScore,
        recentAwards: List<LootAward>,
        decayFactor: Double = 0.9,
    ): FlpsScore {
        val decayMultiplier = Math.pow(decayFactor, recentAwards.size.toDouble())
        return FlpsScore.of((baseScore.value * decayMultiplier).coerceIn(0.0, 1.0))
    }

    fun canRevokeLootAward(
        lootAward: LootAward,
        maxRevocationDays: Int = 7,
        now: Instant = Instant.now(),
    ): Boolean {
        if (!lootAward.isActive()) return false
        val deadline = lootAward.awardedAt.plusSeconds(maxRevocationDays.toLong() * 24 * 60 * 60)
        return now.isBefore(deadline)
    }
}
