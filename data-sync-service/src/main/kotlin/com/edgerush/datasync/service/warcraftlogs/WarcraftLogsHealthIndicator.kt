package com.edgerush.datasync.service.warcraftlogs

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component("warcraftLogs")
class WarcraftLogsHealthIndicator(
    private val configService: WarcraftLogsConfigService,
    private val reportRepository: com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsReportRepository,
) : HealthIndicator {
    override fun health(): Health {
        return try {
            val guilds = configService.getAllEnabledGuilds()

            if (guilds.isEmpty()) {
                return Health.up()
                    .withDetail("status", "No guilds configured")
                    .withDetail("enabled_guilds", 0)
                    .build()
            }

            val guildStatuses =
                guilds.associate { guild ->
                    val lastReport = reportRepository.findByGuildIdOrderBySyncedAtDesc(guild.guildId).firstOrNull()
                    val lastSyncTime = lastReport?.syncedAt
                    val timeSinceSync = lastSyncTime?.let { Duration.between(it, Instant.now()) }

                    val status =
                        when {
                            lastSyncTime == null -> "NEVER_SYNCED"
                            timeSinceSync != null && timeSinceSync.toHours() > 24 -> "STALE"
                            else -> "OK"
                        }

                    guild.guildId to
                        mapOf(
                            "lastSync" to (lastSyncTime?.toString() ?: "Never"),
                            "status" to status,
                            "hoursSinceSync" to (timeSinceSync?.toHours() ?: -1),
                        )
                }

            val hasStaleGuilds = guildStatuses.values.any { it["status"] == "STALE" }
            val hasNeverSynced = guildStatuses.values.any { it["status"] == "NEVER_SYNCED" }

            val overallHealth =
                when {
                    hasNeverSynced -> Health.down()
                    hasStaleGuilds -> Health.status("DEGRADED")
                    else -> Health.up()
                }

            overallHealth
                .withDetail("enabled_guilds", guilds.size)
                .withDetail("guilds", guildStatuses)
                .build()
        } catch (ex: Exception) {
            Health.down()
                .withDetail("error", ex.message)
                .withException(ex)
                .build()
        }
    }
}
