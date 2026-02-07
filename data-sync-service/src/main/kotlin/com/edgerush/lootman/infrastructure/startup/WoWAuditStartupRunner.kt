package com.edgerush.lootman.infrastructure.startup

import com.edgerush.datasync.config.SyncProperties
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

/**
 * Application runner that triggers WoWAudit roster sync on startup.
 * Runs after WowDataStartupRunner (Order 20) to ensure WoW class data is available.
 *
 * Controlled by sync.run-on-startup property (default: false).
 * Set SYNC_RUN_ON_STARTUP=true in environment to enable.
 */
@Component
@Order(20)
class WoWAuditStartupRunner(
    private val wowAuditRosterSyncService: WoWAuditRosterSyncService,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val syncProperties: SyncProperties,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(WoWAuditStartupRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        if (!syncProperties.runOnStartup) {
            logger.info("WoWAudit sync on startup is disabled (sync.run-on-startup=false)")
            return
        }

        logger.info("Starting WoWAudit sync on application startup...")

        try {
            // Get all guilds with WoWAudit configured (get up to 100 guilds)
            val guilds =
                guildConfigurationRepository.findAll(offset = 0, limit = 100)
                    .filter { it.syncEnabled && !it.wowauditGuildUri.isNullOrBlank() }

            if (guilds.isEmpty()) {
                logger.info("No guilds with WoWAudit sync enabled found, skipping startup sync")
                return
            }

            logger.info("Found ${guilds.size} guild(s) with WoWAudit sync enabled")

            for (guild in guilds) {
                try {
                    logger.info("Syncing WoWAudit roster for guild: ${guild.guildName} (${guild.guildId})")
                    val result =
                        wowAuditRosterSyncService.syncRoster(guild.guildId)
                            .subscribeOn(Schedulers.boundedElastic())
                            .block()

                    if (result != null && result.success) {
                        logger.info(
                            "WoWAudit sync completed for guild ${guild.guildName}: " +
                                "created=${result.created}, updated=${result.updated}, skipped=${result.skipped}",
                        )
                    } else {
                        logger.warn(
                            "WoWAudit sync completed with errors for guild ${guild.guildName}: ${result?.error}",
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to sync WoWAudit roster for guild ${guild.guildName}: ${e.message}", e)
                }
            }

            logger.info("WoWAudit startup sync completed")
        } catch (e: Exception) {
            // Don't fail startup if sync fails
            logger.error("WoWAudit startup sync failed: ${e.message}", e)
        }
    }
}
