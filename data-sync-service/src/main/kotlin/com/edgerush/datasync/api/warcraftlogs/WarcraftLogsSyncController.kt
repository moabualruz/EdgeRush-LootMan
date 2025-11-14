package com.edgerush.datasync.api.warcraftlogs

import com.edgerush.datasync.service.warcraftlogs.SyncResult
import com.edgerush.datasync.service.warcraftlogs.WarcraftLogsSyncService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/warcraft-logs")
class WarcraftLogsSyncController(
    private val syncService: WarcraftLogsSyncService,
    private val performanceService: com.edgerush.datasync.service.warcraftlogs.WarcraftLogsPerformanceService,
    private val reportRepository: com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsReportRepository,
) {
    @PostMapping("/sync/{guildId}")
    fun triggerSync(
        @PathVariable guildId: String,
    ): SyncResult =
        runBlocking {
            syncService.syncReportsForGuild(guildId)
        }

    @GetMapping("/sync/{guildId}/status")
    fun getSyncStatus(
        @PathVariable guildId: String,
    ): Map<String, Any> {
        val lastReport = reportRepository.findByGuildIdOrderBySyncedAtDesc(guildId).firstOrNull()
        return mapOf(
            "guildId" to guildId,
            "lastSyncTime" to (lastReport?.syncedAt?.toString() ?: "Never"),
            "lastReportCode" to (lastReport?.reportCode ?: "N/A"),
        )
    }

    @GetMapping("/performance/{guildId}/{characterName}")
    fun getCharacterPerformance(
        @PathVariable guildId: String,
        @PathVariable characterName: String,
        @RequestParam(required = false) realm: String?,
    ): Map<String, Any> {
        val characterRealm = realm ?: ""
        val metrics = performanceService.getPerformanceMetrics(characterName, characterRealm, guildId)
        val mas = performanceService.getMASForCharacter(characterName, characterRealm, guildId)

        return if (metrics != null) {
            mapOf(
                "characterName" to characterName,
                "characterRealm" to characterRealm,
                "mas" to mas,
                "deathsPerAttempt" to metrics.deathsPerAttempt,
                "averageAvoidableDamage" to metrics.averageAvoidableDamagePercentage,
                "totalAttempts" to metrics.totalAttempts,
                "totalDeaths" to metrics.totalDeaths,
                "fightCount" to metrics.fightCount,
            )
        } else {
            mapOf(
                "characterName" to characterName,
                "characterRealm" to characterRealm,
                "error" to "No performance data found",
            )
        }
    }

    @GetMapping("/reports/{guildId}")
    fun getReports(
        @PathVariable guildId: String,
    ): List<Map<String, Any>> {
        return reportRepository.findByGuildIdOrderBySyncedAtDesc(guildId).take(20).map {
            mapOf<String, Any>(
                "code" to it.reportCode,
                "title" to (it.title ?: ""),
                "startTime" to it.startTime.toString(),
                "endTime" to it.endTime.toString(),
                "owner" to (it.owner ?: ""),
                "syncedAt" to it.syncedAt.toString(),
            )
        }
    }
}
