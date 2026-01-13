package com.edgerush.lootman.domain.flps.model

import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

/**
 * Value object representing aggregated raider performance data from Warcraft Logs.
 *
 * Contains deaths and avoidable damage metrics used for MAS calculation.
 */
data class RaiderPerformanceData(
    val raiderId: RaiderId,
    val characterName: String,
    val characterRealm: String,
    /** Total deaths across all analyzed fights */
    val totalDeaths: Int,
    /** Total fights analyzed */
    val totalFights: Int,
    /** Average deaths per fight */
    val deathsPerAttempt: Double,
    /** Average avoidable damage taken as percentage of spec average */
    val avoidableDamagePercentage: Double,
    /** Time period start for this data */
    val periodStart: Instant,
    /** Time period end for this data */
    val periodEnd: Instant,
) {
    init {
        require(totalDeaths >= 0) { "Total deaths cannot be negative" }
        require(totalFights >= 0) { "Total fights cannot be negative" }
        require(deathsPerAttempt >= 0.0) { "Deaths per attempt cannot be negative" }
        require(avoidableDamagePercentage >= 0.0) { "Avoidable damage percentage cannot be negative" }
    }

    companion object {
        /**
         * Creates RaiderPerformanceData from raw metrics.
         */
        fun create(
            raiderId: RaiderId,
            characterName: String,
            characterRealm: String,
            totalDeaths: Int,
            totalFights: Int,
            avoidableDamagePercentage: Double,
            periodStart: Instant,
            periodEnd: Instant,
        ): RaiderPerformanceData {
            val deathsPerAttempt = if (totalFights > 0) {
                totalDeaths.toDouble() / totalFights
            } else {
                0.0
            }

            return RaiderPerformanceData(
                raiderId = raiderId,
                characterName = characterName,
                characterRealm = characterRealm,
                totalDeaths = totalDeaths,
                totalFights = totalFights,
                deathsPerAttempt = deathsPerAttempt,
                avoidableDamagePercentage = avoidableDamagePercentage,
                periodStart = periodStart,
                periodEnd = periodEnd,
            )
        }

        /**
         * Creates empty performance data when no Warcraft Logs data is available.
         */
        fun empty(
            raiderId: RaiderId,
            characterName: String,
            characterRealm: String,
        ): RaiderPerformanceData = RaiderPerformanceData(
            raiderId = raiderId,
            characterName = characterName,
            characterRealm = characterRealm,
            totalDeaths = 0,
            totalFights = 0,
            deathsPerAttempt = 0.0,
            avoidableDamagePercentage = 0.0,
            periodStart = Instant.now(),
            periodEnd = Instant.now(),
        )
    }
}
