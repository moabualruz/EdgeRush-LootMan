package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service to sync historical data (gear, statistics) from WoWAudit API.
 *
 * WoWAudit provides historical data via /v1/historical_data?period={periodId} endpoint
 * which includes per-character gear and statistics snapshots.
 */
@Service
class WoWAuditHistoricalDataSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val raiderGearItemRepository: RaiderGearItemRepository,
    private val raiderStatisticsRepository: RaiderStatisticsRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditHistoricalDataSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs historical data (gear/statistics) from WoWAudit for a specific guild and period.
     *
     * @param guildId The internal guild ID
     * @param periodId The WoWAudit period ID
     * @return Sync result with counts
     */
    fun syncHistoricalData(
        guildId: String,
        periodId: Long,
    ): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit historical data sync for guild={}, periodId={}", guildId, periodId)

        return wowAuditClient.fetchHistoricalData(periodId, guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncHistoricalData(body, guildId) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit historical data sync completed for guild={}, period={}: created={}, updated={}, skipped={}",
                    guildId,
                    periodId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit historical data sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncHistoricalData(
        body: String,
        guildId: String,
    ): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            // WoWAudit historical data response can be an array of characters
            val charactersNode =
                when {
                    node.has("characters") -> node.get("characters")
                    node.isArray -> node
                    else -> {
                        logger.warn("WoWAudit historical data response has unexpected structure")
                        return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                    }
                }

            if (!charactersNode.isArray) {
                logger.warn("WoWAudit historical data characters is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            for (element in charactersNode) {
                try {
                    val characterName =
                        element.path("name").asText("")
                            .ifBlank { element.path("character_name").asText("") }
                    val characterRealm =
                        element.path("realm").asText("")
                            .ifBlank { element.path("character_realm").asText("") }

                    if (characterName.isBlank()) {
                        skipped++
                        continue
                    }

                    // Find raider by character name and realm
                    val raider = raiderEntityRepository.findByCharacterNameAndRealmNormalized(characterName, characterRealm)
                    if (raider == null) {
                        logger.debug("Raider not found for historical data: {} - {}", characterName, characterRealm)
                        skipped++
                        continue
                    }

                    val raiderId = raider.id
                    if (raiderId == null) {
                        skipped++
                        continue
                    }

                    var wasUpdated = false

                    // Process gear items
                    val gearNode = element.path("gear")
                    if (!gearNode.isMissingNode && !gearNode.isNull) {
                        processGearItems(gearNode, raiderId)
                        wasUpdated = true
                    }

                    // Process statistics
                    val statisticsNode = element.path("statistics")
                    if (!statisticsNode.isMissingNode && !statisticsNode.isNull) {
                        processStatistics(statisticsNode, element, raiderId)
                        wasUpdated = true
                    }

                    if (wasUpdated) {
                        updated++
                    } else {
                        skipped++
                    }
                } catch (ex: Exception) {
                    val name = element.path("name").asText("unknown")
                    logger.warn("Failed to process historical data for {}: {}", name, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit historical data response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun processGearItems(
        gearNode: JsonNode,
        raiderId: Long,
    ) {
        // Delete existing gear items for this raider
        val existingItems = raiderGearItemRepository.findByRaiderId(raiderId, 0, 1000)
        existingItems.forEach { item -> item.id?.let { raiderGearItemRepository.delete(it) } }

        val slots =
            listOf(
                "head", "neck", "shoulder", "back", "chest", "wrist",
                "hands", "waist", "legs", "feet", "finger_1", "finger_2",
                "trinket_1", "trinket_2", "main_hand", "off_hand",
            )

        for (setName in listOf("equipped", "best", "spark")) {
            val setNode = gearNode.path(setName)
            if (setNode.isMissingNode || setNode.isNull) continue

            for (slot in slots) {
                val slotNode = setNode.path(slot)
                if (!slotNode.isObject) continue

                val itemId = slotNode.path("item_id").asLong(-1).takeIf { it > 0 }
                val itemLevel = slotNode.path("item_level").asIntOrNull()
                val quality = slotNode.path("quality").asIntOrNull()
                val enchant = slotNode.path("enchant").asText(null)?.takeIf { it.isNotBlank() }
                val enchantQuality = slotNode.path("enchant_quality").asIntOrNull()
                val upgradeLevel = slotNode.path("upgrade_level").asIntOrNull()
                val sockets = slotNode.path("sockets").asIntOrNull()
                val name = slotNode.path("name").asText(null)?.takeIf { it.isNotBlank() }

                if (itemId == null && itemLevel == null && name == null) continue

                raiderGearItemRepository.save(
                    RaiderGearItemEntity(
                        raiderId = raiderId,
                        gearSet = setName,
                        slot = slot,
                        itemId = itemId,
                        itemLevel = itemLevel,
                        quality = quality,
                        enchant = enchant,
                        enchantQuality = enchantQuality,
                        upgradeLevel = upgradeLevel,
                        sockets = sockets,
                        name = name,
                    ),
                )
            }
        }
    }

    private fun processStatistics(
        statisticsNode: JsonNode,
        element: JsonNode,
        raiderId: Long,
    ) {
        val mythicPlusScore = statisticsNode.path("mplus_score").asDoubleOrNull()
        val weeklyHighest = statisticsNode.path("weekly_highest_mplus").asIntOrNull()
        val seasonHighest = statisticsNode.path("season_highest_mplus").asIntOrNull()

        val worldQuestsNode =
            statisticsNode.path("worldQuests").takeIf { it.isObject }
                ?: statisticsNode.path("world_quests").takeIf { it.isObject }
        val worldQuestsTotal = worldQuestsNode?.path("done_total")?.asIntOrNull()
        val worldQuestsThisWeek = worldQuestsNode?.path("this_week")?.asIntOrNull()

        val collectiblesNode = element.path("collectibles")
        val collectiblesMounts = collectiblesNode.path("mounts").asIntOrNull()
        val collectiblesToys = collectiblesNode.path("toys").asIntOrNull()
        val collectiblesUniquePets = collectiblesNode.path("unique_pets").asIntOrNull()
        val collectiblesLevel25Pets = collectiblesNode.path("lvl_25_pets").asIntOrNull()

        val honorLevel = statisticsNode.path("pvp").path("honor_level").asIntOrNull()

        val existingStats = raiderStatisticsRepository.findByRaiderId(raiderId)
        val statsEntity =
            RaiderStatisticsEntity(
                id = existingStats?.id,
                raiderId = raiderId,
                mythicPlusScore = mythicPlusScore,
                weeklyHighestMplus = weeklyHighest,
                seasonHighestMplus = seasonHighest,
                worldQuestsTotal = worldQuestsTotal,
                worldQuestsThisWeek = worldQuestsThisWeek,
                collectiblesMounts = collectiblesMounts,
                collectiblesToys = collectiblesToys,
                collectiblesUniquePets = collectiblesUniquePets,
                collectiblesLevel25Pets = collectiblesLevel25Pets,
                honorLevel = honorLevel,
            )
        raiderStatisticsRepository.save(statsEntity)
    }

    private fun JsonNode.asIntOrNull(): Int? {
        return if (this.isNumber) this.asInt() else null
    }

    private fun JsonNode.asDoubleOrNull(): Double? {
        return if (this.isNumber) this.asDouble() else null
    }
}
