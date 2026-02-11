package com.edgerush.lootman.infrastructure.startup

import com.edgerush.datasync.config.SyncProperties
import com.edgerush.lootman.application.guild.WoWAuditApplicationsSyncService
import com.edgerush.lootman.application.guild.WoWAuditAttendanceSyncService
import com.edgerush.lootman.application.guild.WoWAuditGuestsSyncService
import com.edgerush.lootman.application.guild.WoWAuditPeriodSyncService
import com.edgerush.lootman.application.guild.WoWAuditRaidsSyncService
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.application.guild.WoWAuditTeamSyncService
import com.edgerush.lootman.application.guild.WoWAuditWishlistSyncService
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

/**
 * Application runner that triggers all WoWAudit syncs on startup.
 * Runs after WowDataStartupRunner (Order 20) to ensure WoW class data is available.
 *
 * Chains: period → team → roster → attendance → wishlists → raids → guests → applications.
 *
 * Controlled by sync.run-on-startup property (default: false).
 * Set SYNC_RUN_ON_STARTUP=true in environment to enable.
 */
@Component
@Order(20)
class WoWAuditStartupRunner(
    private val wowAuditPeriodSyncService: WoWAuditPeriodSyncService,
    private val wowAuditTeamSyncService: WoWAuditTeamSyncService,
    private val wowAuditRosterSyncService: WoWAuditRosterSyncService,
    private val wowAuditAttendanceSyncService: WoWAuditAttendanceSyncService,
    private val wowAuditWishlistSyncService: WoWAuditWishlistSyncService,
    private val wowAuditRaidsSyncService: WoWAuditRaidsSyncService,
    private val wowAuditGuestsSyncService: WoWAuditGuestsSyncService,
    private val wowAuditApplicationsSyncService: WoWAuditApplicationsSyncService,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val syncProperties: SyncProperties,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(WoWAuditStartupRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        if (!syncProperties.runOnStartup) {
            logger.info("WoWAudit sync on startup is disabled (sync.run-on-startup=false)")
            return
        }

        logger.info("Starting WoWAudit full sync on application startup...")

        try {
            val guilds =
                guildConfigurationRepository.findAll(offset = 0, limit = 100)
                    .filter { it.syncEnabled && !it.wowauditGuildUri.isNullOrBlank() }

            if (guilds.isEmpty()) {
                logger.info("No guilds with WoWAudit sync enabled found, skipping startup sync")
                return
            }

            logger.info("Found ${guilds.size} guild(s) with WoWAudit sync enabled")

            for (guild in guilds) {
                val guildId = guild.guildId
                val guildName = guild.guildName
                try {
                    logger.info("Starting full WoWAudit sync for guild: {} ({})", guildName, guildId)

                    // 1. Period
                    runSyncSafe("Period", guildName) {
                        wowAuditPeriodSyncService.syncPeriod(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 2. Team
                    runSyncSafe("Team", guildName) {
                        wowAuditTeamSyncService.syncTeam(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 3. Roster
                    runSyncSafe("Roster", guildName) {
                        wowAuditRosterSyncService.syncRoster(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 4. Attendance
                    runSyncSafe("Attendance", guildName) {
                        wowAuditAttendanceSyncService.syncAttendance(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 5. Wishlists
                    runSyncSafe("Wishlists", guildName) {
                        wowAuditWishlistSyncService.syncWishlists(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 6. Raids
                    runSyncSafe("Raids", guildName) {
                        wowAuditRaidsSyncService.syncRaids(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 7. Guests
                    runSyncSafe("Guests", guildName) {
                        wowAuditGuestsSyncService.syncGuests(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }
                    // 8. Applications
                    runSyncSafe("Applications", guildName) {
                        wowAuditApplicationsSyncService.syncApplications(guildId)
                            .subscribeOn(Schedulers.boundedElastic()).block()
                    }

                    logger.info("WoWAudit full sync completed for guild {}", guildName)
                } catch (e: Exception) {
                    logger.error("WoWAudit full sync failed for guild {}: {}", guildName, e.message, e)
                }
            }

            logger.info("WoWAudit startup sync completed")
        } catch (e: Exception) {
            // Don't fail startup if sync fails
            logger.error("WoWAudit startup sync failed: ${e.message}", e)
        }
    }

    private fun runSyncSafe(syncName: String, guildName: String, block: () -> Any?) {
        try {
            val result = block()
            logger.info("  {} sync completed for guild {}: {}", syncName, guildName, result)
        } catch (e: Exception) {
            logger.warn("  {} sync failed for guild {}: {}", syncName, guildName, e.message)
        }
    }
}
