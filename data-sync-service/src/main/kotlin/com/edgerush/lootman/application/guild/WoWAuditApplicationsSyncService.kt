package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.ApplicationEntitySpringRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Service to sync guild applications from WoWAudit API.
 *
 * WoWAudit provides applications via /v1/applications endpoint which includes
 * applicant details, status, and main character information.
 */
@Service
class WoWAuditApplicationsSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val applicationRepository: ApplicationEntitySpringRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditApplicationsSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs application data from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncApplications(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit applications sync for guild={}", guildId)

        return wowAuditClient.fetchApplications(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncApplications(body) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit applications sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit applications sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncApplications(body: String): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            val appsArray =
                when {
                    node.isArray -> node
                    node.has("applications") -> node.get("applications")
                    else -> {
                        logger.warn("WoWAudit applications response has unexpected structure")
                        return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                    }
                }

            if (!appsArray.isArray) {
                logger.warn("WoWAudit applications data is not an array")
                return WoWAuditSyncResult(0, 0, 0, "Invalid response format")
            }

            for (element in appsArray) {
                try {
                    val appId = element.path("id").asLong(-1).takeIf { it > 0 }
                    if (appId == null) {
                        skipped++
                        continue
                    }

                    val appliedAt = element.path("applied_at").asTextOrNull()?.parseOffsetDateTime()
                    val status = element.path("status").asText(null)?.takeIf { it.isNotBlank() }
                    val role = element.path("role").asText(null)?.takeIf { it.isNotBlank() }
                    val age = element.path("age").asIntOrNull()
                    val country = element.path("country").asText(null)?.takeIf { it.isNotBlank() }
                    val battletag = element.path("battletag").asText(null)?.takeIf { it.isNotBlank() }
                    val discordId = element.path("discord_id").asText(null)?.takeIf { it.isNotBlank() }

                    // Main character info can be nested
                    val mainChar = element.path("main_character")
                    val mainCharName =
                        if (mainChar.isObject) mainChar.path("name").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_name").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharRealm =
                        if (mainChar.isObject) mainChar.path("realm").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_realm").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharClass =
                        if (mainChar.isObject) mainChar.path("class").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_class").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharRole =
                        if (mainChar.isObject) mainChar.path("role").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_role").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharRace =
                        if (mainChar.isObject) mainChar.path("race").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_race").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharFaction =
                        if (mainChar.isObject) mainChar.path("faction").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_faction").asText(null)?.takeIf { it.isNotBlank() }
                    val mainCharLevel =
                        if (mainChar.isObject) mainChar.path("level").asIntOrNull()
                        else element.path("main_character_level").asIntOrNull()
                    val mainCharRegion =
                        if (mainChar.isObject) mainChar.path("region").asText(null)?.takeIf { it.isNotBlank() }
                        else element.path("main_character_region").asText(null)?.takeIf { it.isNotBlank() }

                    val existing = applicationRepository.findById(appId).orElse(null)

                    val entity =
                        ApplicationEntity(
                            applicationId = appId,
                            appliedAt = appliedAt,
                            status = status,
                            role = role,
                            age = age,
                            country = country,
                            battletag = battletag,
                            discordId = discordId,
                            mainCharacterName = mainCharName,
                            mainCharacterRealm = mainCharRealm,
                            mainCharacterClass = mainCharClass,
                            mainCharacterRole = mainCharRole,
                            mainCharacterRace = mainCharRace,
                            mainCharacterFaction = mainCharFaction,
                            mainCharacterLevel = mainCharLevel,
                            mainCharacterRegion = mainCharRegion,
                            syncedAt = OffsetDateTime.now(),
                        )

                    applicationRepository.save(entity)

                    if (existing != null) {
                        updated++
                    } else {
                        created++
                    }
                } catch (ex: Exception) {
                    val appId = element.path("id").asText("unknown")
                    logger.warn("Failed to process application {}: {}", appId, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit applications response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun JsonNode.asIntOrNull(): Int? {
        return if (this.isNumber) this.asInt() else null
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
