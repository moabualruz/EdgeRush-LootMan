package com.edgerush.lootman.infrastructure.startup

import com.edgerush.datasync.config.SyncProperties
import com.edgerush.lootman.application.wow.WowDataSyncService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Application runner that syncs WoW classes and specializations on startup.
 * Runs early (Order 10) to ensure data is available for other components.
 *
 * Controlled by sync.run-on-startup property (default: false).
 * Set SYNC_RUN_ON_STARTUP=true in environment to enable.
 */
@Component
@Order(10)
class WowDataStartupRunner(
    private val wowDataSyncService: WowDataSyncService,
    private val syncProperties: SyncProperties,
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(WowDataStartupRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        if (!syncProperties.runOnStartup) {
            logger.info("WoW data sync on startup is disabled (sync.run-on-startup=false)")
            return
        }

        logger.info("Starting WoW data sync on application startup...")

        try {
            // Check if we already have data
            val existingClasses = wowDataSyncService.getAllClasses()

            if (existingClasses.isEmpty()) {
                logger.info("No WoW classes found in database, performing initial sync...")
                val result = wowDataSyncService.syncAllClassesAndSpecs()

                if (result.success) {
                    logger.info("Initial WoW data sync completed successfully: ${result.totalClasses} classes, ${result.totalSpecs} specs")
                } else {
                    logger.warn("WoW data sync completed with errors: ${result.errors}")
                }
            } else {
                logger.info("Found ${existingClasses.size} existing WoW classes, skipping initial sync")
            }
        } catch (e: Exception) {
            // Don't fail startup if sync fails - the system can work without this data
            logger.error("Failed to sync WoW data on startup: ${e.message}", e)
        }
    }
}
