package com.edgerush.datasync.service

import com.edgerush.datasync.entity.BenchmarkMode
import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.repository.GuildConfigurationRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class GuildManagementService(
    private val guildConfigRepository: GuildConfigurationRepository,
    private val scoreCalculator: ScoreCalculator,
    private val modifierService: FlpsModifierService,
    private val behavioralScoreService: BehavioralScoreService
) {
    
    /**
     * Get guild configuration, creating default if not exists
     */
    fun getGuildConfiguration(guildId: String): GuildConfigurationEntity {
        return guildConfigRepository.findByGuildIdAndIsActive(guildId, true)
            ?: createDefaultGuildConfiguration(guildId)
    }
    
    /**
     * Calculate perfect score benchmarks for a guild
     */
    fun calculatePerfectScoreBenchmarks(guildId: String): PerfectScoreBenchmark {
        val guildConfig = getGuildConfiguration(guildId)
        val flpsConfig = modifierService.getGuildModifiers(guildId)
        
        return when (BenchmarkMode.valueOf(guildConfig.benchmarkMode)) {
            BenchmarkMode.THEORETICAL -> calculateTheoreticalPerfect(flpsConfig)
            BenchmarkMode.TOP_PERFORMER -> calculateTopPerformerBenchmark(guildId, flpsConfig)
            BenchmarkMode.CUSTOM -> calculateCustomBenchmark(guildConfig, flpsConfig)
        }
    }
    
    /**
     * Calculate FLPS with percentage scores relative to perfect benchmark
     */
    /**
     * Calculate comprehensive FLPS report with scores, percentages, and behavioral data
     */
    fun calculateComprehensiveFlpsReport(guildId: String): List<ComprehensiveFlpsReport> {
        val standardScores = scoreCalculator.calculateWithRealData(guildId)
        val benchmark = calculatePerfectScoreBenchmarks(guildId)
        val flpsConfig = modifierService.getGuildModifiers(guildId)
        
        return standardScores.map { score ->
            // Get behavioral breakdown for this character
            val behavioralBreakdown = behavioralScoreService.getBehavioralBreakdown(guildId, score.name)
            
            // Determine overall eligibility (includes loot ban check)
            val eligibilityReasons = mutableListOf<String>()
            var eligible = score.eligible
            
            if (behavioralBreakdown.lootBanInfo.isBanned) {
                eligible = false
                eligibilityReasons.add("Character is banned from loot: ${behavioralBreakdown.lootBanInfo.reason}")
            }
            
            if (!score.eligible) {
                eligibilityReasons.add("Character does not meet base FLPS eligibility requirements")
            }
            
            ComprehensiveFlpsReport(
                // Core FLPS Breakdown (Raw Scores)
                breakdown = score,
                
                // RMS Component Scores & Percentages
                attendanceScore = score.acs,
                attendancePercentage = calculatePercentage(score.acs, 1.0),
                mechanicalScore = score.mas,
                mechanicalPercentage = calculatePercentage(score.mas, 1.0),
                preparationScore = score.eps,
                preparationPercentage = calculatePercentage(score.eps, 1.0),
                rmsScore = score.rms,
                rmsPercentage = calculatePercentage(score.rms, benchmark.perfectRms),
                
                // IPI Component Scores & Percentages
                upgradeValueScore = score.upgradeValue,
                upgradeValuePercentage = calculatePercentage(score.upgradeValue, 1.0),
                tierBonusScore = score.tierBonus,
                tierBonusPercentage = calculatePercentage(score.tierBonus, 1.0),
                roleMultiplierScore = score.roleMultiplier,
                roleMultiplierPercentage = calculatePercentage(score.roleMultiplier, getMaxRoleMultiplier(flpsConfig)),
                ipiScore = score.ipi,
                ipiPercentage = calculatePercentage(score.ipi, benchmark.perfectIpi),
                
                // Behavioral Component (New)
                behavioralScore = behavioralBreakdown.behavioralScore,
                behavioralPercentage = behavioralBreakdown.behavioralScore * 100.0, // Convert to percentage
                behavioralBreakdown = behavioralBreakdown,
                
                // Final Calculation Scores & Percentages
                rdfScore = score.rdf,
                rdfPercentage = calculatePercentage(score.rdf, 1.0),
                flpsScore = score.flps,
                flpsPercentage = calculatePercentage(score.flps, benchmark.perfectFlps),
                
                // Meta Information
                benchmarkUsed = benchmark.benchmarkType,
                eligible = eligible,
                eligibilityReasons = eligibilityReasons.toList()
            )
        }
    }
    
    private fun calculateTheoreticalPerfect(flpsConfig: FlpsGuildConfig): PerfectScoreBenchmark {
        // Theoretical perfect: all components at 1.0
        val perfectRms = (1.0 * flpsConfig.rmsWeights.attendance) +
                        (1.0 * flpsConfig.rmsWeights.mechanical) +
                        (1.0 * flpsConfig.rmsWeights.preparation)
        
        val perfectIpi = (1.0 * flpsConfig.ipiWeights.upgradeValue) +
                        (1.0 * flpsConfig.ipiWeights.tierBonus) +
                        (flpsConfig.roleMultipliers.tank * flpsConfig.ipiWeights.roleMultiplier) // Use highest role multiplier
        
        val perfectFlps = perfectRms * perfectIpi * 1.0 // Perfect RDF = 1.0
        
        return PerfectScoreBenchmark(
            perfectRms = perfectRms,
            perfectIpi = perfectIpi,
            perfectFlps = perfectFlps,
            benchmarkType = "THEORETICAL"
        )
    }
    
    private fun calculateTopPerformerBenchmark(guildId: String, flpsConfig: FlpsGuildConfig): PerfectScoreBenchmark {
        val allScores = scoreCalculator.calculateWithRealData(guildId)
        
        if (allScores.isEmpty()) {
            return calculateTheoreticalPerfect(flpsConfig) // Fallback
        }
        
        val maxRms = allScores.maxOfOrNull { it.rms } ?: 1.0
        val maxIpi = allScores.maxOfOrNull { it.ipi } ?: 1.0
        val maxFlps = allScores.maxOfOrNull { it.flps } ?: 1.0
        
        return PerfectScoreBenchmark(
            perfectRms = maxRms,
            perfectIpi = maxIpi,
            perfectFlps = maxFlps,
            benchmarkType = "TOP_PERFORMER"
        )
    }
    
    private fun calculateCustomBenchmark(guildConfig: GuildConfigurationEntity, flpsConfig: FlpsGuildConfig): PerfectScoreBenchmark {
        val customRms = guildConfig.customBenchmarkRms?.toDouble() ?: 1.0
        val customIpi = guildConfig.customBenchmarkIpi?.toDouble() ?: 1.0
        val customFlps = customRms * customIpi * 1.0
        
        return PerfectScoreBenchmark(
            perfectRms = customRms,
            perfectIpi = customIpi,
            perfectFlps = customFlps,
            benchmarkType = "CUSTOM"
        )
    }
    
    private fun calculatePercentage(actual: Double, perfect: Double): Double {
        if (perfect <= 0.0) return 0.0
        return minOf(100.0, maxOf(0.0, (actual / perfect) * 100.0))
    }
    
    private fun getMaxRoleMultiplier(flpsConfig: FlpsGuildConfig): Double {
        return maxOf(flpsConfig.roleMultipliers.tank, flpsConfig.roleMultipliers.healer, flpsConfig.roleMultipliers.dps)
    }
    
    private fun createDefaultGuildConfiguration(guildId: String): GuildConfigurationEntity {
        val defaultConfig = GuildConfigurationEntity(
            guildId = guildId,
            guildName = "Guild $guildId",
            guildDescription = "Auto-created guild configuration",
            wowauditApiKeyEncrypted = null,
            wowauditGuildUri = null,
            benchmarkMode = "THEORETICAL",
            lastSyncAt = null,
            lastSyncStatus = null,
            lastSyncError = null,
            customBenchmarkRms = null,
            customBenchmarkIpi = null,
            benchmarkUpdatedAt = null
        )
        
        return guildConfigRepository.save(defaultConfig)
    }
}

data class PerfectScoreBenchmark(
    val perfectRms: Double,
    val perfectIpi: Double,
    val perfectFlps: Double,
    val benchmarkType: String
)

data class ComprehensiveFlpsReport(
    // Core FLPS Breakdown (Raw Scores)
    val breakdown: com.edgerush.datasync.model.FlpsBreakdown,
    
    // RMS Component Scores & Percentages
    val attendanceScore: Double,            // Raw ACS score (0.0-1.0)
    val attendancePercentage: Double,       // ACS as % of perfect attendance (100%)
    val mechanicalScore: Double,            // Raw MAS score (0.0-1.0)
    val mechanicalPercentage: Double,       // MAS as % of perfect mechanical skill (100%)
    val preparationScore: Double,           // Raw EPS score (0.0-1.0)
    val preparationPercentage: Double,      // EPS as % of perfect preparation (100%)
    val rmsScore: Double,                   // Raw RMS score
    val rmsPercentage: Double,              // Overall RMS as % of perfect RMS
    
    // IPI Component Scores & Percentages  
    val upgradeValueScore: Double,          // Raw upgrade value score (0.0-1.0)
    val upgradeValuePercentage: Double,     // Upgrade value as % of perfect upgrade (100%)
    val tierBonusScore: Double,             // Raw tier bonus score (0.0-1.0)
    val tierBonusPercentage: Double,        // Tier bonus as % of perfect tier bonus (100%)
    val roleMultiplierScore: Double,        // Raw role multiplier value
    val roleMultiplierPercentage: Double,   // Role multiplier as % of max role multiplier
    val ipiScore: Double,                   // Raw IPI score
    val ipiPercentage: Double,              // Overall IPI as % of perfect IPI
    
    // Behavioral Component (New)
    val behavioralScore: Double,            // Behavioral score (1.0 unless deductions applied)
    val behavioralPercentage: Double,       // Always 100% of raw behavioral score
    val behavioralBreakdown: BehavioralBreakdown, // Detailed behavioral information
    
    // Final Calculation Scores & Percentages
    val rdfScore: Double,                   // Raw RDF score (0.0-1.0)
    val rdfPercentage: Double,              // RDF as % of perfect (100% = no recent loot penalty)
    val flpsScore: Double,                  // Final raw FLPS score
    val flpsPercentage: Double,             // Final FLPS as % of theoretical perfect
    
    // Meta Information
    val benchmarkUsed: String,              // "THEORETICAL", "TOP_PERFORMER", or "CUSTOM"
    val eligible: Boolean,                  // Overall eligibility (includes loot ban check)
    val eligibilityReasons: List<String>    // Reasons for ineligibility if any
)