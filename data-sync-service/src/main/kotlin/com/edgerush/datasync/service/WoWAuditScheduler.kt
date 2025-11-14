package com.edgerush.datasync.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WoWAuditScheduler(
    private val syncService: WoWAuditSyncService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${sync.cron}")
    fun scheduledSync() {
        try {
            syncService.syncRoster()
                .then(syncService.syncLootHistory())
                .then(syncService.syncWishlists())
                .then(syncService.syncSupplementalData())
                .block()
        } catch (ex: Exception) {
            log.error("Scheduled WoWAudit sync failed", ex)
        }
    }
}
