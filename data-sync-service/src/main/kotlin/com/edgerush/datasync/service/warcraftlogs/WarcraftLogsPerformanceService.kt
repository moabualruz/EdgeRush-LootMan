package com.edgerush.datasync.service.warcraftlogs

import com.edgerush.datasync.model.warcraftlogs.PerformanceMetrics
import com.edgerush.datasync.model.warcraftlogs.SpecAverages
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsPerformanceRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import kotlin.math.max
import kotlin.math.min

@Service
class WarcraftLogsPerformanceService(
    private val performanceRepository: WarcraftLogsPerformanceRepository,
    private val configService: WarcraftLogsConfigService,
    private val metrics: WarcraftLogsMetrics
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @Cacheable(value = ["warcraftLogsMAS"], key = "#guildId + ':' + #characterName + ':' + #characterRealm")
    fun getMASForCharacter(
        characterName: String,
        characterRealm: String,
        guildId: String
    ): Double {
        val config = configService.getConfig(guildId)
        
        // Get performance metrics for character
        val metrics = getPerformanceMetrics(
            characterName,
            characterRealm,
            guildId,
            Duration.ofDays(config.syncTimeWindowDays.toLong())
        )
        
        if (metrics == null || metrics.fightCount < config.minimumSampleSize) {
            logger.debug("Insufficient data for $characterName-$characterRealm, using fallback MAS: ${config.fallbackMAS}")
            return config.fallbackMAS
        }
        
        // Get spec averages for normalization
        val specAverages = calculateSpecAverages(
            guildId,
            metrics.characterName, // Using as spec placeholder
            config.specAveragePercentile
        )
        
        // Calculate DPA and ADT ratios
        val dpaRatio = calculateDPARatio(metrics.deathsPerAttempt, specAverages.averageDPA)
        val adtRatio = calculateADTRatio(metrics.averageAvoidableDamagePercentage, specAverages.averageADT)
        
        // Check critical threshold
        if (dpaRatio > config.criticalThreshold || adtRatio > config.criticalThreshold) {
            logger.warn("Critical threshold exceeded for $characterName-$characterRealm (DPA: $dpaRatio, ADT: $adtRatio)")
            return 0.0
        }
        
        // Calculate MAS
        val penalty = ((dpaRatio - 1.0) * config.dpaWeight) + ((adtRatio - 1.0) * config.adtWeight)
        val mas = max(0.0, min(1.0, 1.0 - penalty))
        
        logger.debug("Calculated MAS for $characterName-$characterRealm: $mas (DPA ratio: $dpaRatio, ADT ratio: $adtRatio)")
        return round(mas)
    }
    
    fun getPerformanceMetrics(
        characterName: String,
        characterRealm: String,
        guildId: String,
        timeWindow: Duration = Duration.ofDays(30)
    ): PerformanceMetrics? {
        val config = configService.getConfig(guildId)
        val since = Instant.now().minus(timeWindow)
        val performances = performanceRepository.findByCharacterSince(characterName, characterRealm, since)
        
        if (performances.isEmpty()) {
            return null
        }
        
        // Apply time-based weighting for recent performance
        val now = Instant.now()
        val recentCutoff = now.minus(Duration.ofDays(config.recentPerformanceDays.toLong()))
        
        var weightedDeaths = 0.0
        var totalWeight = 0.0
        var weightedAdt = 0.0
        
        performances.forEach { perf ->
            val weight = if (perf.calculatedAt.isAfter(recentCutoff)) {
                config.recentPerformanceWeightMultiplier
            } else {
                1.0
            }
            
            weightedDeaths += perf.deaths * weight
            weightedAdt += perf.avoidableDamagePercentage * weight
            totalWeight += weight
        }
        
        val dpa = if (totalWeight > 0) weightedDeaths / totalWeight else 0.0
        val avgAdt = if (totalWeight > 0) weightedAdt / totalWeight else 0.0
        
        return PerformanceMetrics(
            characterName = characterName,
            characterRealm = characterRealm,
            totalDeaths = performances.sumOf { it.deaths },
            totalAttempts = performances.size,
            deathsPerAttempt = dpa,
            averageAvoidableDamagePercentage = avgAdt,
            fightCount = performances.size
        )
    }
    
    fun calculateDPARatio(characterDPA: Double, specAverageDPA: Double): Double {
        if (specAverageDPA == 0.0) return 1.0
        return characterDPA / specAverageDPA
    }
    
    fun calculateADTRatio(characterADT: Double, specAverageADT: Double): Double {
        if (specAverageADT == 0.0) return 1.0
        return characterADT / specAverageADT
    }
    
    fun calculateSpecAverages(
        guildId: String,
        spec: String,
        percentile: Int = 50
    ): SpecAverages {
        val config = configService.getConfig(guildId)
        val since = Instant.now().minus(Duration.ofDays(config.syncTimeWindowDays.toLong()))
        val performances = performanceRepository.findBySpecSince(spec, since)
        
        if (performances.size < config.minimumSampleSize) {
            logger.debug("Insufficient data for spec $spec, using fallback values")
            return SpecAverages(
                spec = spec,
                sampleSize = 0,
                averageDeathsPerAttempt = config.fallbackDPA,
                averageAvoidableDamagePercentage = config.fallbackADT,
                percentile = percentile
            )
        }
        
        // Calculate percentile values
        val dpaValues = performances.map { it.deaths.toDouble() }.sorted()
        val adtValues = performances.map { it.avoidableDamagePercentage }.sorted()
        
        val dpaPercentile = calculatePercentile(dpaValues, percentile)
        val adtPercentile = calculatePercentile(adtValues, percentile)
        
        return SpecAverages(
            spec = spec,
            sampleSize = performances.size,
            averageDeathsPerAttempt = dpaPercentile,
            averageAvoidableDamagePercentage = adtPercentile,
            percentile = percentile
        )
    }
    
    private fun calculatePercentile(sortedValues: List<Double>, percentile: Int): Double {
        if (sortedValues.isEmpty()) return 0.0
        
        val index = (percentile / 100.0) * (sortedValues.size - 1)
        val lower = sortedValues[index.toInt()]
        val upper = if (index.toInt() + 1 < sortedValues.size) {
            sortedValues[index.toInt() + 1]
        } else {
            lower
        }
        
        val fraction = index - index.toInt()
        return lower + (upper - lower) * fraction
    }
    
    private fun round(value: Double, places: Int = 3): Double {
        val factor = Math.pow(10.0, places.toDouble())
        return kotlin.math.round(value * factor) / factor
    }
}
