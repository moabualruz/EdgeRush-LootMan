package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.raider.repository.RaiderPvpBracketRepository
import com.edgerush.lootman.domain.raider.repository.RaiderRaidProgressRepository
import com.edgerush.lootman.domain.raider.repository.RaiderRenownRepository
import com.edgerush.lootman.domain.raider.repository.RaiderTrackItemRepository
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Service to sync guild roster from WoWAudit API.
 *
 * WoWAudit provides detailed raider data including gear, statistics, and PvP brackets.
 * This is complementary to Battle.net roster sync - WoWAudit focuses on raiders who are
 * tracked for raid purposes, while Battle.net provides all guild members.
 */
@Service
class WoWAuditRosterSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val raiderGearItemRepository: RaiderGearItemRepository,
    private val raiderStatisticsRepository: RaiderStatisticsRepository,
    private val raiderTrackItemRepository: RaiderTrackItemRepository,
    private val raiderCrestCountRepository: RaiderCrestCountRepository,
    private val raiderVaultSlotRepository: RaiderVaultSlotRepository,
    private val raiderRenownRepository: RaiderRenownRepository,
    private val raiderRaidProgressRepository: RaiderRaidProgressRepository,
    private val raiderPvpBracketRepository: RaiderPvpBracketRepository,
    private val raiderWarcraftLogRepository: RaiderWarcraftLogRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditRosterSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs raiders from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncRoster(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit roster sync for guild={}", guildId)

        // Use guild's bnet_region as fallback if WoWAudit doesn't provide region
        val fallbackRegion = guildConfig.bnetRegion ?: "eu"

        return fetchTeamInfo(guildConfig.wowauditApiKeyEncrypted)
            .flatMap { teamInfo ->
                wowAuditClient.fetchRoster(guildConfig.wowauditApiKeyEncrypted)
                    .map { body -> parseAndSyncRoster(body, guildId, teamInfo, fallbackRegion) }
            }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit roster sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit roster sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun fetchTeamInfo(apiKey: String?): Mono<TeamInfo> =
        wowAuditClient.fetchTeam(apiKey)
            .map { body ->
                try {
                    val node = objectMapper.readTree(body)
                    TeamInfo(
                        region = node.path("region").asText(""),
                        realm = node.path("realm").asText(""),
                    )
                } catch (ex: Exception) {
                    logger.warn("Failed to parse team info: {}", ex.message)
                    TeamInfo("", "")
                }
            }
            .onErrorResume { ex ->
                logger.warn("Failed to fetch team info: {}", ex.message)
                Mono.just(TeamInfo("", ""))
            }

    private fun parseAndSyncRoster(
        body: String,
        guildId: String,
        teamInfo: TeamInfo,
        fallbackRegion: String,
    ): WoWAuditSyncResult {
        // Use WoWAudit team region if available, otherwise use guild's bnet_region
        val defaultRegion = teamInfo.region.ifBlank { fallbackRegion }
        val defaultRealm = teamInfo.realm

        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)
            if (!node.isArray) {
                logger.warn("WoWAudit roster response is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            for (element in node) {
                try {
                    val result = processRaider(element, guildId, defaultRegion, defaultRealm)
                    when (result) {
                        UpsertResult.CREATED -> created++
                        UpsertResult.UPDATED -> updated++
                        UpsertResult.SKIPPED -> skipped++
                    }
                } catch (ex: Exception) {
                    val name = element.path("name").asText("unknown")
                    logger.warn("Failed to process raider {}: {}", name, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit roster response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun processRaider(
        element: JsonNode,
        guildId: String,
        defaultRegion: String,
        defaultRealm: String,
    ): UpsertResult {
        val name = element.path("name").asText("")
        if (name.isBlank()) return UpsertResult.SKIPPED

        val realm = element.path("realm").asText(defaultRealm)
        val region = element.path("region").asText(defaultRegion)
        val wowauditId = element.path("id").asLong(-1).takeIf { it > 0 }
        val clazz = element.path("class").asText("")
        val spec = element.path("spec").asText("")
        val rawRole = element.path("role").asText("")
        val role = normalizeRole(rawRole)
        val rank = element.path("rank").asText(null)?.takeIf { it.isNotBlank() }
        val status = element.path("status").asText(null)?.takeIf { it.isNotBlank() }
        val note = element.path("note").asText(null)?.takeIf { it.isNotBlank() }
        val blizzardId = element.path("blizzard_id").asLong(-1).takeIf { it > 0 }
        val trackingSince = parseOffsetDateTime(element.path("tracking_since").asText(null))
        val joinDate = parseOffsetDateTime(element.path("timestamps").path("join_date").asText(null))
        val blizzardLastModified = parseOffsetDateTime(element.path("timestamps").path("blizzard_last_modified").asText(null))

        val now = OffsetDateTime.now()

        // Find existing raider by wowauditId first, then by name+realm
        var existing = wowauditId?.let { raiderEntityRepository.findByWowauditId(it) }
        if (existing == null) {
            existing = raiderEntityRepository.findByCharacterNameAndRealm(name, realm)
        }

        val isNew = existing == null
        val entity =
            if (existing != null) {
                existing.copy(
                    region = region.ifBlank { existing.region },
                    guildId = guildId,
                    wowauditId = wowauditId ?: existing.wowauditId,
                    clazz = clazz.ifBlank { existing.clazz },
                    spec = spec.ifBlank { existing.spec },
                    role = role.ifBlank { existing.role },
                    rank = rank ?: existing.rank,
                    status = status ?: existing.status,
                    note = note ?: existing.note,
                    blizzardId = blizzardId ?: existing.blizzardId,
                    trackingSince = trackingSince ?: existing.trackingSince,
                    joinDate = joinDate ?: existing.joinDate,
                    blizzardLastModified = blizzardLastModified ?: existing.blizzardLastModified,
                    lastSync = now,
                )
            } else {
                RaiderEntity(
                    characterName = name,
                    realm = realm,
                    region = region,
                    guildId = guildId,
                    wowauditId = wowauditId,
                    clazz = clazz,
                    spec = spec,
                    role = role,
                    rank = rank,
                    status = status,
                    note = note,
                    blizzardId = blizzardId,
                    trackingSince = trackingSince ?: now,
                    joinDate = joinDate,
                    blizzardLastModified = blizzardLastModified,
                    lastSync = now,
                )
            }

        val saved = raiderEntityRepository.save(entity)
        val raiderId = saved.id ?: return if (isNew) UpsertResult.CREATED else UpsertResult.UPDATED

        // Process gear items
        processGearItems(element, raiderId)

        // Process statistics
        processStatistics(element, raiderId)

        return if (isNew) UpsertResult.CREATED else UpsertResult.UPDATED
    }

    private fun processGearItems(
        element: JsonNode,
        raiderId: Long,
    ) {
        val gearNode = element.path("gear")
        if (gearNode.isMissingNode || gearNode.isNull) return

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
        element: JsonNode,
        raiderId: Long,
    ) {
        val statisticsNode = element.path("statistics")
        if (statisticsNode.isMissingNode || statisticsNode.isNull) return

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

    private fun JsonNode.asDoubleOrNull(): Double? {
        return if (this.isNumber) this.asDouble() else null
    }

    /**
     * Normalize WoWAudit role names to FLPS domain roles.
     *
     * WoWAudit uses: Melee, Ranged, Heal, Tank
     * FLPS expects: DPS, HEALER, TANK
     */
    private fun normalizeRole(wowauditRole: String): String {
        return when (wowauditRole.lowercase()) {
            "melee", "ranged" -> "DPS"
            "heal", "healer" -> "HEALER"
            "tank" -> "TANK"
            else -> "DPS" // Default to DPS for unknown/empty roles
        }
    }

    private enum class UpsertResult {
        CREATED,
        UPDATED,
        SKIPPED,
    }

    private data class TeamInfo(
        val region: String,
        val realm: String,
    )
}

data class WoWAuditSyncResult(
    val created: Int,
    val updated: Int,
    val skipped: Int,
    val error: String?,
) {
    val total: Int get() = created + updated + skipped
    val success: Boolean get() = error == null
}
