package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.PeriodSnapshotEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Sync result that also carries the discovered periodId and seasonId
 * for use by downstream sync services (loot history, historical data).
 */
data class PeriodSyncResult(
    val syncResult: WoWAuditSyncResult,
    val currentPeriodId: Long? = null,
    val currentSeasonId: Long? = null,
)

/**
 * Service to sync period/season information from WoWAudit API.
 *
 * WoWAudit provides period info via /v1/period endpoint which includes
 * the current period_id, season_id, and related metadata.
 * This data is a prerequisite for loot history and historical data syncs.
 */
@Service
class WoWAuditPeriodSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val periodSnapshotRepository: PeriodSnapshotEntitySpringRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditPeriodSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs period data from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return PeriodSyncResult with sync counts and discovered period/season IDs
     */
    fun syncPeriod(guildId: String): Mono<PeriodSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit period sync for guild={}", guildId)

        return wowAuditClient.fetchPeriod(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncPeriod(body) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit period sync completed for guild={}: periodId={}, seasonId={}, created={}, updated={}",
                    guildId,
                    result.currentPeriodId,
                    result.currentSeasonId,
                    result.syncResult.created,
                    result.syncResult.updated,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit period sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncPeriod(body: String): PeriodSyncResult {
        var created = 0
        var updated = 0
        var currentPeriodId: Long? = null
        var currentSeasonId: Long? = null

        try {
            val node = objectMapper.readTree(body)

            // Extract current period and season
            val periodId = node.path("current_period").asLongOrNull()
            val seasonNode = node.path("current_season")
            val seasonId = seasonNode.path("id").asLongOrNull()
                ?: node.path("season_id").asLongOrNull()
            val teamId = node.path("team_id").asLongOrNull()

            currentPeriodId = periodId
            currentSeasonId = seasonId

            if (periodId == null && seasonId == null) {
                logger.warn("WoWAudit period response has no period_id or season_id")
                return PeriodSyncResult(
                    WoWAuditSyncResult(0, 0, 1, "No period or season data found"),
                    null,
                    null,
                )
            }

            // Check for existing snapshot
            val existing =
                if (teamId != null && seasonId != null && periodId != null) {
                    periodSnapshotRepository.findByTeamIdAndSeasonIdAndPeriodId(teamId, seasonId, periodId)
                } else {
                    null
                }

            val entity =
                PeriodSnapshotEntity(
                    id = existing?.id,
                    teamId = teamId,
                    seasonId = seasonId,
                    periodId = periodId,
                    currentPeriod = periodId,
                    fetchedAt = OffsetDateTime.now(),
                )

            periodSnapshotRepository.save(entity)

            if (existing != null) {
                updated++
            } else {
                created++
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit period response: {}", ex.message, ex)
            return PeriodSyncResult(
                WoWAuditSyncResult(created, updated, 0, ex.message),
                currentPeriodId,
                currentSeasonId,
            )
        }

        return PeriodSyncResult(
            WoWAuditSyncResult(created, updated, 0, null),
            currentPeriodId,
            currentSeasonId,
        )
    }

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }
}
