package com.edgerush.datasync.service.raidbots

import com.edgerush.datasync.client.raidbots.RaidbotsClient
import com.edgerush.datasync.entity.raidbots.RaidbotsResultEntity
import com.edgerush.datasync.entity.raidbots.RaidbotsSimulationEntity
import com.edgerush.datasync.repository.raidbots.RaidbotsResultRepository
import com.edgerush.datasync.repository.raidbots.RaidbotsSimulationRepository
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RaidbotsSimulationService(
    private val client: RaidbotsClient,
    private val simulationRepository: RaidbotsSimulationRepository,
    private val resultRepository: RaidbotsResultRepository,
    private val configService: RaidbotsConfigService,
    private val profileGenerator: SimProfileGeneratorService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Stores a raider-submitted Droptimizer report URL
     * Raiders run their own sims on Raidbots.com and submit the report URL
     */
    suspend fun storeDroptimizerReport(
        guildId: String,
        characterName: String,
        characterRealm: String,
        reportUrl: String
    ): RaidbotsSimulationEntity {
        logger.info("Storing Droptimizer report for $characterName-$characterRealm: $reportUrl")
        
        if (!client.isValidReportUrl(reportUrl)) {
            throw IllegalArgumentException("Invalid Raidbots report URL: $reportUrl")
        }
        
        val reportId = reportUrl.substringAfterLast("/")
        
        // Save simulation entity
        val entity = RaidbotsSimulationEntity(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
            simId = reportId,
            status = "PENDING",
            submittedAt = Instant.now(),
            completedAt = null,
            profile = reportUrl,
            simOptions = "{}"
        )
        
        val saved = simulationRepository.save(entity)
        
        // Parse the report asynchronously
        try {
            processDroptimizerReport(reportId)
        } catch (ex: Exception) {
            logger.error("Failed to parse Droptimizer report: $reportId", ex)
        }
        
        return saved
    }
    
    private suspend fun processDroptimizerReport(reportId: String) {
        val simulation = simulationRepository.findBySimId(reportId)
            ?: throw IllegalArgumentException("Simulation not found: $reportId")
        
        try {
            // Parse the public Droptimizer report
            val results = client.parseDroptimizerReport("${client.properties.baseUrl}/simbot/report/$reportId")
            
            // Save results
            results.results.values.forEach { itemResult ->
                val resultEntity = RaidbotsResultEntity(
                    simulationId = simulation.id!!,
                    itemId = itemResult.itemId,
                    itemName = itemResult.itemName,
                    slot = itemResult.slot,
                    dpsGain = itemResult.dpsGain,
                    percentGain = itemResult.percentGain,
                    calculatedAt = Instant.now()
                )
                resultRepository.save(resultEntity)
            }
            
            // Mark simulation as completed
            val completed = simulation.copy(
                status = "COMPLETED",
                completedAt = Instant.now()
            )
            simulationRepository.save(completed)
            
            logger.info("Processed ${results.results.size} results for Droptimizer report: $reportId")
        } catch (ex: Exception) {
            logger.error("Failed to process Droptimizer report: $reportId", ex)
            val failed = simulation.copy(status = "FAILED")
            simulationRepository.save(failed)
        }
    }
}
