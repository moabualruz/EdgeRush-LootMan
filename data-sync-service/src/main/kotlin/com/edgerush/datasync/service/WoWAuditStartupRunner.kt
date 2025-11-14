package com.edgerush.datasync.service

import com.edgerush.datasync.config.SyncProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class WoWAuditStartupRunner(
    private val properties: SyncProperties,
    private val syncService: WoWAuditSyncService,
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (!properties.runOnStartup) {
            return
        }
        try {
            syncService.syncRoster()
                .then(syncService.syncLootHistory())
                .then(syncService.syncWishlists())
                .then(syncService.syncSupplementalData())
                .block()
        } catch (ex: Exception) {
            log.error("Startup WoWAudit sync failed", ex)
        }
    }
}
