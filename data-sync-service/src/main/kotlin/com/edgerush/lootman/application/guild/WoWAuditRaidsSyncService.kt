package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.RaidEncounterEntitySpringRepository
import com.edgerush.lootman.infrastructure.springdata.RaidEntitySpringRepository
import com.edgerush.lootman.infrastructure.springdata.RaidSignupEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Service to sync raids (including signups and encounters) from WoWAudit API.
 *
 * WoWAudit provides:
 * - /v1/raids?include_past=true — list of all raids with basic info
 * - /v1/raids/{id} — detailed raid info including signups and encounters
 *
 * This service fetches the raid list, then fetches details for each raid
 * to populate the raids, raid_signups, and raid_encounters tables.
 */
@Service
class WoWAuditRaidsSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val raidRepository: RaidEntitySpringRepository,
    private val raidSignupRepository: RaidSignupEntitySpringRepository,
    private val raidEncounterRepository: RaidEncounterEntitySpringRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditRaidsSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs raids (with signups and encounters) from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncRaids(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit raids sync for guild={}", guildId)

        return wowAuditClient.fetchRaids(includePast = true, apiKey = guildConfig.wowauditApiKeyEncrypted)
            .flatMap { body -> parseAndSyncRaids(body, guildConfig.wowauditApiKeyEncrypted) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit raids sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit raids sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncRaids(
        body: String,
        apiKey: String?,
    ): Mono<WoWAuditSyncResult> {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            val raidsArray =
                when {
                    node.isArray -> node
                    node.has("raids") -> node.get("raids")
                    else -> {
                        logger.warn("WoWAudit raids response has unexpected structure")
                        return Mono.just(WoWAuditSyncResult(0, 0, 0, "Unexpected response structure"))
                    }
                }

            if (!raidsArray.isArray) {
                logger.warn("WoWAudit raids data is not an array")
                return Mono.just(WoWAuditSyncResult(0, 0, 0, "Invalid response format"))
            }

            for (element in raidsArray) {
                try {
                    val raidId = element.path("id").asLong(-1).takeIf { it > 0 }
                    if (raidId == null) {
                        skipped++
                        continue
                    }

                    val date = element.path("date").asText(null)?.parseLocalDate()
                    val startTime = element.path("start_time").asText(null)?.parseLocalTime()
                    val endTime = element.path("end_time").asText(null)?.parseLocalTime()
                    val instance = element.path("instance").asText(null)?.takeIf { it.isNotBlank() }
                    val difficulty = element.path("difficulty").asText(null)?.takeIf { it.isNotBlank() }
                    val optional = element.path("optional").asBooleanOrNull()
                    val status = element.path("status").asText(null)?.takeIf { it.isNotBlank() }
                    val presentSize = element.path("present_size").asIntOrNull()
                    val totalSize = element.path("total_size").asIntOrNull()
                    val notes = element.path("notes").asText(null)?.takeIf { it.isNotBlank() }
                    val selectionsImage = element.path("selections_image").asText(null)?.takeIf { it.isNotBlank() }
                    val teamId = element.path("team_id").asLongOrNull()
                    val seasonId = element.path("season_id").asLongOrNull()
                    val periodId = element.path("period_id").asLongOrNull()

                    val existing = raidRepository.findByRaidId(raidId)

                    val entity =
                        RaidEntity(
                            raidId = raidId,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            instance = instance,
                            difficulty = difficulty,
                            optional = optional,
                            status = status,
                            presentSize = presentSize,
                            totalSize = totalSize,
                            notes = notes,
                            selectionsImage = selectionsImage,
                            teamId = teamId,
                            seasonId = seasonId,
                            periodId = periodId,
                            createdAt = existing?.createdAt,
                            updatedAt = OffsetDateTime.now(),
                            syncedAt = OffsetDateTime.now(),
                        )

                    raidRepository.save(entity)

                    // Process signups and encounters from raid detail
                    processRaidDetail(element, raidId, apiKey)

                    if (existing != null) {
                        updated++
                    } else {
                        created++
                    }
                } catch (ex: Exception) {
                    val rId = element.path("id").asText("unknown")
                    logger.warn("Failed to process raid {}: {}", rId, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit raids response: {}", ex.message, ex)
            return Mono.just(WoWAuditSyncResult(created, updated, skipped, ex.message))
        }

        return Mono.just(WoWAuditSyncResult(created, updated, skipped, null))
    }

    /**
     * Processes signups and encounters from raid detail data.
     * The raid list response may already include signups/encounters inline,
     * or we may need to fetch the detail endpoint.
     */
    private fun processRaidDetail(
        raidNode: JsonNode,
        raidId: Long,
        apiKey: String?,
    ) {
        // Process signups if present
        val signupsNode = raidNode.path("signups")
        if (signupsNode.isArray && signupsNode.size() > 0) {
            processSignups(signupsNode, raidId)
        }

        // Process encounters if present
        val encountersNode = raidNode.path("encounters")
        if (encountersNode.isArray && encountersNode.size() > 0) {
            processEncounters(encountersNode, raidId)
        }

        // Process characters (some APIs use "characters" instead of "signups")
        val charsNode = raidNode.path("characters")
        if (charsNode.isArray && charsNode.size() > 0 && !signupsNode.isArray) {
            processSignups(charsNode, raidId)
        }
    }

    private fun processSignups(
        signupsNode: JsonNode,
        raidId: Long,
    ) {
        // Clear existing signups for this raid
        raidSignupRepository.deleteByRaidId(raidId)

        for (signup in signupsNode) {
            try {
                val characterId = signup.path("character_id").asLongOrNull()
                    ?: signup.path("id").asLongOrNull()
                val characterName = signup.path("character_name").asText(null)
                    ?: signup.path("name").asText(null)
                val characterRealm = signup.path("character_realm").asText(null)
                    ?: signup.path("realm").asText(null)
                val characterRegion = signup.path("character_region").asText(null)
                    ?: signup.path("region").asText(null)
                val characterClass = signup.path("character_class").asText(null)
                    ?: signup.path("class").asText(null)
                val characterRole = signup.path("character_role").asText(null)
                    ?: signup.path("role").asText(null)
                val characterGuest = signup.path("guest").asBooleanOrNull()
                    ?: signup.path("character_guest").asBooleanOrNull()
                val signupStatus = signup.path("status").asText(null)?.takeIf { it.isNotBlank() }
                val comment = signup.path("comment").asText(null)?.takeIf { it.isNotBlank() }
                val selected = signup.path("selected").asBooleanOrNull()

                raidSignupRepository.save(
                    RaidSignupEntity(
                        raidId = raidId,
                        characterId = characterId,
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterRegion = characterRegion,
                        characterClass = characterClass,
                        characterRole = characterRole,
                        characterGuest = characterGuest,
                        status = signupStatus,
                        comment = comment,
                        selected = selected,
                    ),
                )
            } catch (ex: Exception) {
                logger.warn("Failed to process signup for raid {}: {}", raidId, ex.message)
            }
        }
    }

    private fun processEncounters(
        encountersNode: JsonNode,
        raidId: Long,
    ) {
        // Clear existing encounters for this raid
        raidEncounterRepository.deleteByRaidId(raidId)

        for (encounter in encountersNode) {
            try {
                val encounterId = encounter.path("id").asLongOrNull()
                    ?: encounter.path("encounter_id").asLongOrNull()
                val name = encounter.path("name").asText(null)?.takeIf { it.isNotBlank() }
                val enabled = encounter.path("enabled").asBooleanOrNull()
                val extra = encounter.path("extra").asBooleanOrNull()
                val notes = encounter.path("notes").asText(null)?.takeIf { it.isNotBlank() }

                raidEncounterRepository.save(
                    RaidEncounterEntity(
                        raidId = raidId,
                        encounterId = encounterId,
                        name = name,
                        enabled = enabled,
                        extra = extra,
                        notes = notes,
                    ),
                )
            } catch (ex: Exception) {
                logger.warn("Failed to process encounter for raid {}: {}", raidId, ex.message)
            }
        }
    }

    // ---- Extension helpers ----

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }

    private fun JsonNode.asIntOrNull(): Int? {
        return if (this.isNumber) this.asInt() else null
    }

    private fun JsonNode.asBooleanOrNull(): Boolean? {
        return if (this.isBoolean) this.asBoolean() else null
    }

    private fun String.parseLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            try {
                LocalDate.parse(this.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun String.parseLocalTime(): LocalTime? {
        return try {
            LocalTime.parse(this)
        } catch (_: Exception) {
            null
        }
    }
}
