package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.lootman.domain.application.client.WarcraftLogsClient
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.OffsetDateTime

@Service
class WarcraftLogsRosterSyncService(
    private val warcraftLogsClient: WarcraftLogsClient,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val raiderWarcraftLogRepository: RaiderWarcraftLogRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val syncRunRepository: com.edgerush.lootman.domain.sync.repository.SyncRunRepository,
) {
    private val logger = LoggerFactory.getLogger(WarcraftLogsRosterSyncService::class.java)

    fun syncRoster(guildId: String): Mono<WarcraftLogsSyncResult> {
        val guildConfig = guildConfigurationRepository.findByGuildId(guildId)
            ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        // Start Sync Run Log
        val syncRun = syncRunRepository.save(
            com.edgerush.datasync.entity.SyncRunEntity(
                source = "WarcraftLogs",
                status = "RUNNING",
                startedAt = OffsetDateTime.now(),
                completedAt = null,
                message = "Starting sync for guild $guildId"
            )
        )

        // Use guild's bnet_region as default
        val region = guildConfig.bnetRegion ?: "eu"

        logger.info("Starting Warcraft Logs roster sync for guild={}", guildId)

        val raiders = raiderEntityRepository.findByGuildId(guildId, 0, 1000)
        
        var created = 0
        var updated = 0
        var skipped = 0
        
        return Flux.fromIterable(raiders)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap { raider ->
                val realm = raider.realm
                val name = raider.characterName
                val raiderId = raider.id!!

                warcraftLogsClient.fetchCharacterParses(
                    region = raider.region ?: region,
                    serverName = realm,
                    characterName = name
                )
                .map { result ->
                    if (result != null) {
                        try {
                            val existingLogs = raiderWarcraftLogRepository.findByRaiderId(raiderId, 0, 100)
                            existingLogs.forEach { log -> 
                                if (log.id != null) raiderWarcraftLogRepository.delete(log.id!!) 
                            }

                            if (result.bestPerformanceAverage != null) {
                                raiderWarcraftLogRepository.save(
                                    RaiderWarcraftLogEntity(
                                        id = null,
                                        raiderId = raiderId,
                                        difficulty = "Best Perf. Avg",
                                        score = result.bestPerformanceAverage.toInt()
                                    )
                                )
                            }

                            if (result.medianPerformanceAverage != null) {
                                raiderWarcraftLogRepository.save(
                                    RaiderWarcraftLogEntity(
                                        id = null,
                                        raiderId = raiderId,
                                        difficulty = "Median Perf. Avg",
                                        score = result.medianPerformanceAverage.toInt()
                                    )
                                )
                            }
                            
                            UpsertResult.UPDATED
                        } catch (e: Exception) {
                            logger.warn("Error saving WCL data for $name: ${e.message}")
                            UpsertResult.SKIPPED
                        }
                    } else {
                        UpsertResult.SKIPPED
                    }
                }
                .defaultIfEmpty(UpsertResult.SKIPPED)
                .onErrorResume { e ->
                    logger.warn("Failed to fetch WCL for $name: ${e.message}")
                    Mono.just(UpsertResult.SKIPPED)
                }
            }
            .sequential()
            .collectList()
            .flatMap { results ->
                created = results.count { it == UpsertResult.CREATED }
                updated = results.count { it == UpsertResult.UPDATED }
                skipped = results.count { it == UpsertResult.SKIPPED }
                
                // Complete Sync Run Log
                syncRunRepository.save(
                    syncRun.copy(
                        status = "COMPLETED",
                        completedAt = OffsetDateTime.now(),
                        message = "Synced ${updated} raiders (Skipped: ${skipped})"
                    )
                )

                Mono.just(WarcraftLogsSyncResult(created, updated, skipped, null))
            }
            .doOnSuccess { result ->
                logger.info("Warcraft Logs roster sync completed for guild={}: updated={}, skipped={}",
                    guildId, result.updated, result.skipped)
                updateGuildSyncStatus(guildConfig, "SUCCESS", null)
            }
            .doOnError { ex ->
                logger.error("Warcraft Logs roster sync failed for guild={}: {}", guildId, ex.message, ex)
                
                // Fail Sync Run Log
                syncRunRepository.save(
                    syncRun.copy(
                        status = "FAILED",
                        completedAt = OffsetDateTime.now(),
                        message = "Failed: ${ex.message}"
                    )
                )
                
                updateGuildSyncStatus(guildConfig, "FAILED", ex.message)
            }
    }



    private fun updateGuildSyncStatus(config: GuildConfigurationEntity, status: String, error: String?) {
        // Reuse bnet fields or maybe we need new fields?
        // For now, let's just log it. Adding fields to Entity might be out of scope for "fixing".
        // Use lastSyncStatus generic fields as fallback or just don't update entity if schema doesn't support WCL specific status
        // There are fields: lastSyncStatus (WoWAudit), bnetLastSyncStatus (Bnet).
        // WCL doesn't have its own status columns in Schema yet unless I add them.
        // I will skip status update DB write for now to avoid schema changes block.
    }

    private enum class UpsertResult {
        CREATED,
        UPDATED,
        SKIPPED
    }
}

data class WarcraftLogsSyncResult(
    val created: Int,
    val updated: Int,
    val skipped: Int,
    val error: String?
) {
    val total: Int get() = created + updated + skipped
    val success: Boolean get() = error == null
}
