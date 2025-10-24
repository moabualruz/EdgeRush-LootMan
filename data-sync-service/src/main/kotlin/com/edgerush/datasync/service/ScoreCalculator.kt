package com.edgerush.datasync.service

import com.edgerush.datasync.model.FlpsBreakdown
import com.edgerush.datasync.model.LootAward
import com.edgerush.datasync.model.LootTier
import com.edgerush.datasync.model.RaiderInput
import com.edgerush.datasync.model.Role
import java.time.Clock
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

class ScoreCalculator(
    private val clock: Clock = Clock.systemUTC()
) {

    fun calculate(inputs: List<RaiderInput>): List<FlpsBreakdown> {
        val referenceDate = LocalDate.now(clock)
        return inputs.map { calculateForRaider(it, referenceDate) }
            .sortedByDescending { it.flps }
    }

    fun calculateForRaider(input: RaiderInput, referenceDate: LocalDate = LocalDate.now(clock)): FlpsBreakdown {
        val acs = attendanceCommitmentScore(input.attendancePercent)
        val mas = mechanicalAdherenceScore(
            input.deathsPerAttempt,
            input.specAverageDpa,
            input.avoidableDamagePct,
            input.specAverageAdt
        )
        val eps = externalPreparationScore(input.vaultSlots, input.crestUsageRatio, input.heroicBossesCleared)
        val rms = (acs * 0.40) + (mas * 0.40) + (eps * 0.20)

        val uv = upgradeValue(input.simulatedGain, input.specBaselineOutput)
        val tierBonus = tierBonus(input.tierPiecesOwned)
        val roleMultiplier = roleMultiplier(input.role)
        val ipi = (uv * 0.45) + (tierBonus * 0.35) + (roleMultiplier * 0.20)

        val rdf = recencyDecayFactor(input.lastAwards, referenceDate)
        val flps = (rms * ipi) * rdf
        val eligible = acs >= 0.8 && mas > 0.0

        return FlpsBreakdown(
            name = input.name,
            role = input.role,
            acs = round(acs),
            mas = round(mas),
            eps = round(eps),
            rms = round(rms),
            upgradeValue = round(uv),
            tierBonus = round(tierBonus),
            roleMultiplier = round(roleMultiplier),
            ipi = round(ipi),
            rdf = round(rdf),
            flps = round(flps),
            eligible = eligible
        )
    }

    private fun attendanceCommitmentScore(attendancePercent: Int): Double = when {
        attendancePercent >= 100 -> 1.0
        attendancePercent >= 80 -> 0.9
        else -> 0.0
    }

    private fun mechanicalAdherenceScore(
        deathsPerAttempt: Double,
        specAvgDpa: Double,
        avoidableDamagePct: Double,
        specAvgAdt: Double
    ): Double {
        val dpaRatio = if (specAvgDpa == 0.0) 0.0 else deathsPerAttempt / specAvgDpa
        val adtRatio = if (specAvgAdt == 0.0) 0.0 else avoidableDamagePct / specAvgAdt

        if (dpaRatio > 1.5 || adtRatio > 1.5) {
            return 0.0
        }

        val penalty = ((dpaRatio - 1.0) * 0.25) + ((adtRatio - 1.0) * 0.25)
        return round(max(0.0, min(1.0, 1 - penalty)))
    }

    private fun externalPreparationScore(vaultSlots: Int, crestUsageRatio: Double, heroicKills: Int): Double {
        val vault = min(1.0, vaultSlots / 3.0)
        val crest = crestUsageRatio
        val heroic = min(1.0, heroicKills / 6.0)
        val eps = (vault * 0.5) + (crest * 0.3) + (heroic * 0.2)
        return round(min(1.0, eps))
    }

    private fun upgradeValue(simulatedGain: Double, specBaseline: Double): Double {
        if (specBaseline <= 0.0) return 0.0
        return round(simulatedGain / specBaseline)
    }

    private fun tierBonus(tierPiecesOwned: Int): Double = when {
        tierPiecesOwned <= 1 -> 1.2
        tierPiecesOwned <= 3 -> 1.1
        else -> 1.0
    }

    private fun roleMultiplier(role: Role): Double = when (role) {
        Role.DPS -> 1.0
        Role.Tank -> 0.8
        Role.Healer -> 0.7
    }

    private fun recencyDecayFactor(lastAwards: List<LootAward>, referenceDate: LocalDate): Double {
        if (lastAwards.isEmpty()) return 1.0

        val mostRecent = lastAwards.maxByOrNull { it.awardedOn } ?: return 1.0
        val weeksSince = kotlin.math.max(0, java.time.temporal.ChronoUnit.WEEKS.between(mostRecent.awardedOn, referenceDate).toInt())

        val base = when (mostRecent.tier) {
            LootTier.A -> 0.8
            LootTier.B -> 0.9
            LootTier.C -> 1.0
        }
        return round(min(1.0, base + (0.1 * weeksSince)))
    }

    private fun round(value: Double, places: Int = 3): Double {
        val factor = Math.pow(10.0, places.toDouble())
        return kotlin.math.round(value * factor) / factor
    }
}
