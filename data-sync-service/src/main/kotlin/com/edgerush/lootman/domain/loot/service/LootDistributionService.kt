package com.edgerush.lootman.domain.loot.service

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
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
        val thresholdTime = now.minusSeconds(decayThresholdDays.toLong() * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE)
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
        val deadline = lootAward.awardedAt.plusSeconds(maxRevocationDays.toLong() * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE)
        return now.isBefore(deadline)
    }

    companion object {
        private const val HOURS_PER_DAY = 24
        private const val MINUTES_PER_HOUR = 60
        private const val SECONDS_PER_MINUTE = 60
    }
}
