package com.edgerush.datasync.service.warcraftlogs

import com.edgerush.datasync.client.warcraftlogs.WarcraftLogsClient
import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsGuildConfig
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsFightEntity
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsPerformanceEntity
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsReportEntity
import com.edgerush.datasync.model.warcraftlogs.SpecAverages
import com.edgerush.datasync.model.warcraftlogs.WarcraftLogsReport
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsFightRepository
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsPerformanceRepository
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsReportRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class WarcraftLogsSyncService(
    private val client: WarcraftLogsClient,
    private val reportRepository: WarcraftLogsReportRepository,
    private val fightRepository: WarcraftLogsFightRepository,
    private val performanceRepository: WarcraftLogsPerformanceRepository,
    private val configService: WarcraftLogsConfigService,
    private val characterMappingService: CharacterMappingService,
    private val metrics: WarcraftLogsMetrics,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun syncReportsForGuild(guildId: String): SyncResult {
        logger.info("Starting Warcraft Logs sync for guild: $guildId")
        val startTime = Instant.now()

        try {
            val config = configService.getConfig(guildId)

            if (!config.enabled) {
                logger.info("Warcraft Logs integration disabled for guild: $guildId")
                return SyncResult(
                    guildId = guildId,
                    success = true,
                    reportsProcessed = 0,
                    fightsProcessed = 0,
                    performanceRecordsCreated = 0,
                    duration = Duration.between(startTime, Instant.now()),
                    message = "Integration disabled",
                )
            }

            val endTime = Instant.now()
            val startTimeWindow = endTime.minus(Duration.ofDays(config.syncTimeWindowDays.toLong()))

            // Fetch reports from Warcraft Logs API
            val reports =
                client.fetchReportsForGuild(
                    guildName = config.guildName,
                    realm = config.realm,
                    region = config.region,
                    startTime = startTimeWindow,
                    endTime = endTime,
                )

            logger.info("Found ${reports.size} reports for guild: $guildId")

            var fightsProcessed = 0
            var performanceRecordsCreated = 0

            // Process each report
            reports.forEach { report ->
                val result = processReport(report, guildId, config)
                fightsProcessed += result.fightsProcessed
                performanceRecordsCreated += result.performanceRecordsCreated
            }

            val duration = Duration.between(startTime, Instant.now())
            logger.info("Completed Warcraft Logs sync for guild: $guildId in ${duration.toMillis()}ms")

            // Record metrics
            metrics.recordSyncSuccess()
            metrics.recordReportsProcessed(reports.size)
            metrics.recordFightsProcessed(fightsProcessed)
            metrics.recordPerformanceRecords(performanceRecordsCreated)

            return SyncResult(
                guildId = guildId,
                success = true,
                reportsProcessed = reports.size,
                fightsProcessed = fightsProcessed,
                performanceRecordsCreated = performanceRecordsCreated,
                duration = duration,
            )
        } catch (ex: Exception) {
            val duration = Duration.between(startTime, Instant.now())
            logger.error("Failed to sync Warcraft Logs for guild: $guildId", ex)

            // Record failure metric
            metrics.recordSyncFailure()

            return SyncResult(
                guildId = guildId,
                success = false,
                reportsProcessed = 0,
                fightsProcessed = 0,
                performanceRecordsCreated = 0,
                duration = duration,
                error = ex.message,
            )
        }
    }

    private suspend fun processReport(
        report: WarcraftLogsReport,
        guildId: String,
        config: WarcraftLogsGuildConfig,
    ): ProcessResult {
        // Check if report already exists
        val existing = reportRepository.findByReportCode(report.code)
        if (existing != null) {
            logger.debug("Report ${report.code} already synced, skipping")
            return ProcessResult(0, 0)
        }

        // Save report metadata
        val reportEntity =
            WarcraftLogsReportEntity(
                guildId = guildId,
                reportCode = report.code,
                title = report.title,
                startTime = Instant.ofEpochMilli(report.startTime),
                endTime = Instant.ofEpochMilli(report.endTime),
                owner = report.owner,
                zone = report.zone,
                syncedAt = Instant.now(),
                rawMetadata = null,
            )

        val savedReport = reportRepository.save(reportEntity)
        logger.debug("Saved report: ${report.code}")

        // Fetch detailed fight data from API
        val fights = client.fetchFightData(report.code)
        logger.debug("Fetched ${fights.size} fights for report: ${report.code}")

        // Process fights
        var fightsProcessed = 0
        var performanceRecordsCreated = 0

        fights.forEach { fight ->
            val difficultyName = getDifficultyName(fight.difficulty)
            if (config.includedDifficulties.contains(difficultyName)) {
                val result = processFight(fight, savedReport.id!!, report.code, guildId, config)
                fightsProcessed++
                performanceRecordsCreated += result.performanceRecordsCreated
            } else {
                logger.debug("Skipping fight ${fight.name} - difficulty $difficultyName not included")
            }
        }

        return ProcessResult(fightsProcessed, performanceRecordsCreated)
    }

    private suspend fun processFight(
        fight: com.edgerush.datasync.model.warcraftlogs.WarcraftLogsFight,
        reportId: Long,
        reportCode: String,
        guildId: String,
        config: WarcraftLogsGuildConfig,
    ): ProcessResult {
        // Check if fight already exists
        val existingFight = fightRepository.findByReportIdAndFightId(reportId, fight.id)
        if (existingFight != null) {
            logger.debug("Fight ${fight.id} already processed, skipping")
            return ProcessResult(0, 0)
        }

        // Save fight entity
        val fightEntity =
            WarcraftLogsFightEntity(
                reportId = reportId,
                fightId = fight.id,
                encounterId = fight.encounterID,
                encounterName = fight.name,
                difficulty = getDifficultyName(fight.difficulty),
                kill = fight.kill,
                startTime = fight.startTime,
                endTime = fight.endTime,
                bossPercentage = fight.bossPercentage,
            )

        val savedFight = fightRepository.save(fightEntity)
        logger.debug("Saved fight: ${fight.name} (${fight.id})")

        // Extract performance metrics for characters
        val performanceRecords =
            extractPerformanceMetrics(
                reportCode = reportCode,
                fightId = fight.id,
                savedFightId = savedFight.id!!,
                guildId = guildId,
            )

        return ProcessResult(0, performanceRecords)
    }

    private suspend fun extractPerformanceMetrics(
        reportCode: String,
        fightId: Int,
        savedFightId: Long,
        guildId: String,
    ): Int {
        var recordsCreated = 0

        try {
            // Get character mappings for this guild
            val mappings = characterMappingService.getAllMappings(guildId)

            // For each mapped character, fetch performance data
            mappings.forEach { mapping ->
                try {
                    val performance =
                        client.fetchCharacterPerformance(
                            reportCode = reportCode,
                            fightId = fightId,
                            characterName = mapping.warcraftLogsName,
                        )

                    if (performance != null) {
                        val avoidablePercentage =
                            if (performance.damageTaken > 0) {
                                (performance.avoidableDamageTaken.toDouble() / performance.damageTaken.toDouble()) * 100.0
                            } else {
                                0.0
                            }

                        val performanceEntity =
                            WarcraftLogsPerformanceEntity(
                                fightId = savedFightId,
                                characterName = mapping.wowauditName,
                                characterRealm = mapping.wowauditRealm,
                                characterClass = performance.`class`,
                                characterSpec = performance.spec,
                                deaths = performance.deaths,
                                damageTaken = performance.damageTaken,
                                avoidableDamageTaken = performance.avoidableDamageTaken,
                                avoidableDamagePercentage = avoidablePercentage,
                                performancePercentile = performance.performancePercentile,
                                itemLevel = performance.itemLevel,
                                calculatedAt = Instant.now(),
                            )

                        performanceRepository.save(performanceEntity)
                        recordsCreated++
                        logger.debug("Saved performance data for ${mapping.wowauditName}")
                    }
                } catch (ex: Exception) {
                    logger.warn("Failed to fetch performance for ${mapping.warcraftLogsName}: ${ex.message}")
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to extract performance metrics for fight $fightId", ex)
        }

        return recordsCreated
    }

    suspend fun calculateSpecAverages(
        guildId: String,
        spec: String,
        percentile: Int = 50,
    ): SpecAverages? {
        try {
            val config = configService.getConfig(guildId)

            // Get all performance data for this spec
            val performanceData = performanceRepository.findByCharacterSpec(spec)

            if (performanceData.size < config.minimumSampleSize) {
                logger.warn("Insufficient data for spec $spec: ${performanceData.size} < ${config.minimumSampleSize}")
                return null
            }

            // Calculate percentile values
            val dpaValues =
                performanceData.map {
                    if (it.damageTaken > 0) it.deaths.toDouble() / (it.damageTaken / 1000000.0) else 0.0
                }.sorted()

            val adtValues = performanceData.map { it.avoidableDamagePercentage }.sorted()

            val dpaPercentile = calculatePercentile(dpaValues, percentile)
            val adtPercentile = calculatePercentile(adtValues, percentile)

            return com.edgerush.datasync.model.warcraftlogs.SpecAverages(
                spec = spec,
                sampleSize = performanceData.size,
                averageDeathsPerAttempt = dpaPercentile,
                averageAvoidableDamagePercentage = adtPercentile,
                percentile = percentile,
            )
        } catch (ex: Exception) {
            logger.error("Failed to calculate spec averages for $spec", ex)
            return null
        }
    }

    private fun calculatePercentile(
        sortedValues: List<Double>,
        percentile: Int,
    ): Double {
        if (sortedValues.isEmpty()) return 0.0

        val index = (percentile / 100.0) * (sortedValues.size - 1)
        val lower = sortedValues[index.toInt()]
        val upper =
            if (index.toInt() + 1 < sortedValues.size) {
                sortedValues[index.toInt() + 1]
            } else {
                lower
            }

        val fraction = index - index.toInt()
        return lower + (upper - lower) * fraction
    }

    private fun getDifficultyName(difficulty: Int): String {
        return when (difficulty) {
            3 -> "Normal"
            4 -> "Heroic"
            5 -> "Mythic"
            else -> "Unknown"
        }
    }

    private data class ProcessResult(
        val fightsProcessed: Int,
        val performanceRecordsCreated: Int,
    )
}

data class SyncResult(
    val guildId: String,
    val success: Boolean,
    val reportsProcessed: Int,
    val fightsProcessed: Int,
    val performanceRecordsCreated: Int,
    val duration: Duration,
    val message: String? = null,
    val error: String? = null,
)
