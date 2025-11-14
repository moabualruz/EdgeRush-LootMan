package com.edgerush.datasync.service.raidbots

import com.edgerush.datasync.repository.raidbots.RaidbotsResultRepository
import com.edgerush.datasync.repository.raidbots.RaidbotsSimulationRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import kotlin.math.min

@Service
class RaidbotsUpgradeService(
    private val simulationRepository: RaidbotsSimulationRepository,
    private val resultRepository: RaidbotsResultRepository,
    private val configService: RaidbotsConfigService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @Cacheable(value = ["raidbotsUV"], key = "#guildId + ':' + #characterName + ':' + #characterRealm + ':' + #itemId")
    fun getUVForItem(
        guildId: String,
        characterName: String,
        characterRealm: String,
        itemId: Long
    ): Double {
        val config = configService.getConfig(guildId)
        
        // Get most recent simulation for character
        val simulations = simulationRepository.findByCharacter(characterName, characterRealm)
            .filter { it.status == "COMPLETED" }
        
        if (simulations.isEmpty()) {
            logger.debug("No simulations found for $characterName-$characterRealm, using fallback")
            return 0.5 // Fallback value
        }
        
        val recentSim = simulations.first()
        
        // Check if simulation is stale
        val age = Duration.between(recentSim.completedAt, Instant.now()).toDays()
        if (age > config.staleSimThresholdDays) {
            logger.warn("Simulation is stale ($age days old) for $characterName-$characterRealm")
        }
        
        // Get result for this item
        val results = resultRepository.findBySimulationId(recentSim.id!!)
        val itemResult = results.find { it.itemId == itemId }
        
        if (itemResult == null) {
            logger.debug("No result found for item $itemId")
            return 0.5
        }
        
        // Normalize UV (percent gain / 100 to get 0-1 range)
        val normalizedUV = min(1.0, itemResult.percentGain / 100.0)
        
        // Apply time-based weighting if recent
        val weightedUV = if (age <= config.recentSimDays) {
            normalizedUV * config.recentSimWeightMultiplier
        } else {
            normalizedUV
        }
        
        return min(1.0, weightedUV)
    }
    
    fun getUpgradeValuesForCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): Map<Long, Double> {
        val simulations = simulationRepository.findByCharacter(characterName, characterRealm)
            .filter { it.status == "COMPLETED" }
        
        if (simulations.isEmpty()) {
            return emptyMap()
        }
        
        val recentSim = simulations.first()
        val results = resultRepository.findBySimulationId(recentSim.id!!)
        
        return results.associate { result ->
            result.itemId to min(1.0, result.percentGain / 100.0)
        }
    }
}
