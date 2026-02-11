package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.GuestEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Service to sync guest characters from WoWAudit API.
 *
 * WoWAudit provides guest info via /v1/guests endpoint which includes
 * guest character details used during raid signups.
 */
@Service
class WoWAuditGuestsSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val guestRepository: GuestEntitySpringRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditGuestsSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs guest data from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncGuests(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit guests sync for guild={}", guildId)

        return wowAuditClient.fetchGuests(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncGuests(body) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit guests sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit guests sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncGuests(body: String): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            val guestsArray =
                when {
                    node.isArray -> node
                    node.has("guests") -> node.get("guests")
                    else -> {
                        logger.warn("WoWAudit guests response has unexpected structure")
                        return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                    }
                }

            if (!guestsArray.isArray) {
                logger.warn("WoWAudit guests data is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            for (element in guestsArray) {
                try {
                    val guestId = element.path("id").asLong(-1).takeIf { it > 0 }
                    if (guestId == null) {
                        skipped++
                        continue
                    }

                    val name = element.path("name").asText("")
                    if (name.isBlank()) {
                        skipped++
                        continue
                    }

                    val realm = element.path("realm").asText(null)?.takeIf { it.isNotBlank() }
                    val clazz = element.path("class").asText(null)?.takeIf { it.isNotBlank() }
                    val role = element.path("role").asText(null)?.takeIf { it.isNotBlank() }
                    val blizzardId = element.path("blizzard_id").asLongOrNull()
                    val trackingSince = element.path("tracking_since").asTextOrNull()?.parseOffsetDateTime()

                    val existing = guestRepository.findByGuestId(guestId)

                    val entity =
                        GuestEntity(
                            guestId = guestId,
                            name = name,
                            realm = realm,
                            clazz = clazz,
                            role = role,
                            blizzardId = blizzardId,
                            trackingSince = trackingSince,
                            syncedAt = OffsetDateTime.now(),
                        )

                    guestRepository.save(entity)

                    if (existing != null) {
                        updated++
                    } else {
                        created++
                    }
                } catch (ex: Exception) {
                    val guestName = element.path("name").asText("unknown")
                    logger.warn("Failed to process guest {}: {}", guestName, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit guests response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
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
