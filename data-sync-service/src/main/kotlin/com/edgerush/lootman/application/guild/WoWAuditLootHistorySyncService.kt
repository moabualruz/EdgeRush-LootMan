package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.infrastructure.springdata.LootAwardEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Service to sync loot history from WoWAudit API.
 *
 * WoWAudit provides loot history via /v1/loot_history/{seasonId} endpoint which includes
 * all loot awards for a specific season.
 */
@Service
class WoWAuditLootHistorySyncService(
    private val wowAuditClient: WoWAuditClient,
    private val lootAwardRepository: LootAwardEntitySpringRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditLootHistorySyncService::class.java)
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs loot history from WoWAudit for a specific guild and season.
     *
     * @param guildId The internal guild ID
     * @param seasonId The WoWAudit season ID
     * @return Sync result with counts
     */
    fun syncLootHistory(guildId: String, seasonId: Long): Mono<WoWAuditSyncResult> {
        val guildConfig = guildConfigurationRepository.findByGuildId(guildId)
            ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit loot history sync for guild={}, seasonId={}", guildId, seasonId)

        return wowAuditClient.fetchLootHistory(seasonId)
            .map { body -> parseAndSyncLootHistory(body, guildId) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit loot history sync completed for guild={}, season={}: created={}, updated={}, skipped={}",
                    guildId, seasonId, result.created, result.updated, result.skipped
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit loot history sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncLootHistory(body: String, guildId: String): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            // WoWAudit loot history response can be an array or have a "loot" field
            val lootNode = when {
                node.has("loot") -> node.get("loot")
                node.isArray -> node
                else -> {
                    logger.warn("WoWAudit loot history response has unexpected structure")
                    return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                }
            }

            if (!lootNode.isArray) {
                logger.warn("WoWAudit loot history is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            for (element in lootNode) {
                try {
                    val result = processLootAward(element, guildId)
                    when (result) {
                        UpsertResult.CREATED -> created++
                        UpsertResult.UPDATED -> updated++
                        UpsertResult.SKIPPED -> skipped++
                    }
                } catch (ex: Exception) {
                    val itemName = element.path("item_name").asText("unknown")
                    logger.warn("Failed to process loot award for item {}: {}", itemName, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit loot history response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun processLootAward(element: JsonNode, guildId: String): UpsertResult {
        // Extract character info
        val characterName = element.path("character").path("name").asText("")
            .ifBlank { element.path("character_name").asText("") }
        val characterRealm = element.path("character").path("realm").asText("")
            .ifBlank { element.path("character_realm").asText("") }

        if (characterName.isBlank()) return UpsertResult.SKIPPED

        // Find raider by character name and realm
        val raider = raiderEntityRepository.findByCharacterNameAndRealmNormalized(characterName, characterRealm)
        if (raider == null) {
            logger.debug("Raider not found for loot award: {} - {}", characterName, characterRealm)
            return UpsertResult.SKIPPED
        }

        val raiderId = raider.id ?: return UpsertResult.SKIPPED

        // Extract item info
        val itemId = element.path("item_id").asLong(-1).takeIf { it > 0 }
            ?: element.path("item").path("id").asLong(-1).takeIf { it > 0 }
            ?: return UpsertResult.SKIPPED

        val itemName = element.path("item_name").asText("")
            .ifBlank { element.path("item").path("name").asText("") }
            .ifBlank { "Unknown Item" }

        // Check for duplicate by rclootcouncil_id if present
        val rclootcouncilId = element.path("rclootcouncil_id").asText(null)?.takeIf { it.isNotBlank() }
        if (rclootcouncilId != null) {
            val existing = lootAwardRepository.findByRclootcouncilId(rclootcouncilId)
            if (existing != null) {
                return UpsertResult.SKIPPED // Already exists
            }
        }

        val awardedAt = parseOffsetDateTime(element.path("awarded_at").asText(null))
            ?: parseOffsetDateTime(element.path("date").asText(null))
            ?: OffsetDateTime.now()

        val entity = LootAwardEntity(
            raiderId = raiderId,
            itemId = itemId,
            itemName = itemName,
            tier = element.path("tier").asText(""),
            flps = element.path("flps").asDouble(0.0),
            rdf = element.path("rdf").asDouble(0.0),
            awardedAt = awardedAt,
            rclootcouncilId = rclootcouncilId,
            icon = element.path("icon").asText(null)?.takeIf { it.isNotBlank() },
            slot = element.path("slot").asText(null)?.takeIf { it.isNotBlank() },
            quality = element.path("quality").asText(null)?.takeIf { it.isNotBlank() },
            responseTypeId = element.path("response_type").path("id").asIntOrNull(),
            responseTypeName = element.path("response_type").path("name").asText(null)?.takeIf { it.isNotBlank() },
            responseTypeRgba = element.path("response_type").path("rgba").asText(null)?.takeIf { it.isNotBlank() },
            responseTypeExcluded = element.path("response_type").path("excluded").booleanValue(),
            propagatedResponseTypeId = element.path("propagated_response_type").path("id").asIntOrNull(),
            propagatedResponseTypeName = element.path("propagated_response_type").path("name").asText(null)?.takeIf { it.isNotBlank() },
            propagatedResponseTypeRgba = element.path("propagated_response_type").path("rgba").asText(null)?.takeIf { it.isNotBlank() },
            propagatedResponseTypeExcluded = element.path("propagated_response_type").path("excluded").booleanValue(),
            sameResponseAmount = element.path("same_response_amount").asIntOrNull(),
            note = element.path("note").asText(null)?.takeIf { it.isNotBlank() },
            wishValue = element.path("wish_value").asIntOrNull(),
            difficulty = element.path("difficulty").asText(null)?.takeIf { it.isNotBlank() },
            discarded = element.path("discarded").booleanValue(),
            characterId = raider.characterId,
            awardedByCharacterId = element.path("awarded_by").path("character_id").asLongOrNull(),
            awardedByName = element.path("awarded_by").path("name").asText(null)?.takeIf { it.isNotBlank() },
        )

        lootAwardRepository.save(entity)
        return UpsertResult.CREATED
    }

    private fun parseOffsetDateTime(text: String?): OffsetDateTime? {
        if (text.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(text)
        } catch (ex: Exception) {
            try {
                OffsetDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME)
            } catch (ex2: Exception) {
                null
            }
        }
    }

    private fun JsonNode.asIntOrNull(): Int? {
        return if (this.isNumber) this.asInt() else null
    }

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }

    private enum class UpsertResult {
        CREATED,
        UPDATED,
        SKIPPED
    }
}
