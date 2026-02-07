package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.WishlistSnapshotEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.wishlist.repository.WishlistSnapshotRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Service to sync wishlists from WoWAudit API.
 *
 * WoWAudit provides wishlist data via /v1/wishlists endpoint which includes
 * per-character wishlist items for upgrade tracking.
 */
@Service
class WoWAuditWishlistSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val wishlistSnapshotRepository: WishlistSnapshotRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditWishlistSyncService::class.java)
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs wishlists from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncWishlists(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig = guildConfigurationRepository.findByGuildId(guildId)
            ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit wishlist sync for guild={}", guildId)

        return wowAuditClient.fetchWishlists(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncWishlists(body, guildId) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit wishlist sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId, result.created, result.updated, result.skipped
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit wishlist sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncWishlists(body: String, guildId: String): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            // WoWAudit wishlist response can be an array of characters with wishlists
            val charactersNode = when {
                node.has("characters") -> node.get("characters")
                node.isArray -> node
                else -> {
                    logger.warn("WoWAudit wishlist response has unexpected structure")
                    return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                }
            }

            if (!charactersNode.isArray) {
                logger.warn("WoWAudit wishlist characters is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            // Extract team, season, and period info if available
            val teamId = node.path("team_id").asLongOrNull()
            val seasonId = node.path("season_id").asLongOrNull()
            val periodId = node.path("period_id").asLongOrNull()

            val now = OffsetDateTime.now()

            for (element in charactersNode) {
                try {
                    val characterName = element.path("name").asText("")
                        .ifBlank { element.path("character_name").asText("") }
                    val characterRealm = element.path("realm").asText("")
                        .ifBlank { element.path("character_realm").asText("") }
                    val characterRegion = element.path("region").asText(null)

                    if (characterName.isBlank() || characterRealm.isBlank()) {
                        skipped++
                        continue
                    }

                    // Find raider to get ID
                    val raider = raiderEntityRepository.findByCharacterNameAndRealmNormalized(characterName, characterRealm)
                    val raiderId = raider?.id

                    // Store the raw wishlist data as a snapshot
                    val wishlistData = element.path("wishlist").takeIf { !it.isMissingNode && !it.isNull }
                        ?: element.path("items").takeIf { !it.isMissingNode && !it.isNull }
                        ?: element

                    // Only store if there's actual wishlist data
                    if (wishlistData.isMissingNode || wishlistData.isNull ||
                        (wishlistData.isArray && wishlistData.isEmpty)
                    ) {
                        skipped++
                        continue
                    }

                    val rawPayload = objectMapper.writeValueAsString(wishlistData)

                    val entity = WishlistSnapshotEntity(
                        raiderId = raiderId,
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterRegion = characterRegion,
                        teamId = teamId,
                        seasonId = seasonId,
                        periodId = periodId,
                        rawPayload = rawPayload,
                        syncedAt = now,
                    )

                    wishlistSnapshotRepository.save(entity)
                    created++
                } catch (ex: Exception) {
                    val name = element.path("name").asText("unknown")
                    logger.warn("Failed to process wishlist for {}: {}", name, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit wishlist response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }
}
