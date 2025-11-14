package com.edgerush.datasync.service.warcraftlogs

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WarcraftLogsScheduler(
    private val syncService: WarcraftLogsSyncService,
    private val configService: WarcraftLogsConfigService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @Scheduled(cron = "\${warcraft-logs.sync.cron:0 0 */6 * * *}")
    fun scheduledSync() = runBlocking {
        logger.info("Starting scheduled Warcraft Logs sync")
        
        val guilds = configService.getAllEnabledGuilds()
        logger.info("Found ${guilds.size} guilds with Warcraft Logs enabled")
        
        guilds.forEach { guild ->
            try {
                val result = syncService.syncReportsForGuild(guild.guildId)
                if (result.success) {
                    logger.info(
                        "Successfully synced guild ${guild.guildId}: " +
                        "${result.reportsProcessed} reports, ${result.fightsProcessed} fights, " +
                        "${result.performanceRecordsCreated} performance records in ${result.duration.toMillis()}ms"
                    )
                } else {
                    logger.error("Failed to sync guild ${guild.guildId}: ${result.error}")
                }
            } catch (ex: Exception) {
                logger.error("Exception during sync for guild ${guild.guildId}", ex)
            }
        }
        
        logger.info("Completed scheduled Warcraft Logs sync")
    }
}
