package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.TeamMetadataEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.TeamMetadataEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Service to sync team metadata from WoWAudit API.
 *
 * WoWAudit provides team info via /v1/team endpoint which includes
 * guild name, region, realm, URL, and refresh timestamps.
 */
@Service
class WoWAuditTeamSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val teamMetadataRepository: TeamMetadataEntitySpringRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditTeamSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs team metadata from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncTeam(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit team sync for guild={}", guildId)

        return wowAuditClient.fetchTeam(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncTeam(body) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit team sync completed for guild={}: created={}, updated={}",
                    guildId,
                    result.created,
                    result.updated,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit team sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncTeam(body: String): WoWAuditSyncResult {
        var created = 0
        var updated = 0

        try {
            val node = objectMapper.readTree(body)

            val teamId = node.path("id").asLong(-1).takeIf { it > 0 }
                ?: node.path("team_id").asLong(-1).takeIf { it > 0 }

            if (teamId == null) {
                logger.warn("WoWAudit team response has no team_id")
                return WoWAuditSyncResult(0, 0, 1, "No team_id in response")
            }

            val guildId = node.path("guild_id").asLongOrNull()
            val guildName = node.path("guild_name").asText(null)?.takeIf { it.isNotBlank() }
            val name = node.path("name").asText(null)?.takeIf { it.isNotBlank() }
            val region = node.path("region").asText(null)?.takeIf { it.isNotBlank() }
            val realm = node.path("realm").asText(null)?.takeIf { it.isNotBlank() }
            val url = node.path("url").asText(null)?.takeIf { it.isNotBlank() }
            val lastRefreshedBlizzard = node.path("last_refreshed_blizzard").asTextOrNull()?.parseOffsetDateTime()
            val lastRefreshedPercentiles = node.path("last_refreshed_percentiles").asTextOrNull()?.parseOffsetDateTime()
            val lastRefreshedMythicPlus = node.path("last_refreshed_mythic_plus").asTextOrNull()?.parseOffsetDateTime()
            val wishlistUpdatedAt = node.path("wishlist_updated_at").asTextOrNull()?.parseOffsetDateTime()

            val existing = teamMetadataRepository.findByTeamId(teamId)

            val entity =
                TeamMetadataEntity(
                    teamId = teamId,
                    guildId = guildId,
                    guildName = guildName,
                    name = name,
                    region = region,
                    realm = realm,
                    url = url,
                    lastRefreshedBlizzard = lastRefreshedBlizzard,
                    lastRefreshedPercentiles = lastRefreshedPercentiles,
                    lastRefreshedMythicPlus = lastRefreshedMythicPlus,
                    wishlistUpdatedAt = wishlistUpdatedAt,
                    syncedAt = OffsetDateTime.now(),
                )

            teamMetadataRepository.save(entity)

            if (existing != null) {
                updated++
            } else {
                created++
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit team response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, 0, ex.message)
        }

        return WoWAuditSyncResult(created, updated, 0, null)
    }

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }

    private fun JsonNode.asTextOrNull(): String? {
        return if (this.isTextual) this.asText() else null
    }

    private fun String.parseOffsetDateTime(): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(this)
        } catch (_: Exception) {
            null
        }
    }
}
