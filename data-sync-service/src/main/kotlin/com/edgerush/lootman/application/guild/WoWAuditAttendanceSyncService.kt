package com.edgerush.lootman.application.guild

import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.domain.attendance.repository.AttendanceStatRepository
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Service to sync attendance data from WoWAudit API.
 *
 * WoWAudit provides attendance data via /v1/attendance endpoint which includes
 * per-character attendance stats for raids.
 */
@Service
class WoWAuditAttendanceSyncService(
    private val wowAuditClient: WoWAuditClient,
    private val attendanceStatRepository: AttendanceStatRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditAttendanceSyncService::class.java)
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Syncs attendance data from WoWAudit for a specific guild.
     *
     * @param guildId The internal guild ID
     * @return Sync result with counts
     */
    fun syncAttendance(guildId: String): Mono<WoWAuditSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        if (guildConfig.wowauditGuildUri.isNullOrBlank()) {
            return Mono.error(IllegalArgumentException("WoWAudit guild URI not configured for guildId=$guildId"))
        }

        logger.info("Starting WoWAudit attendance sync for guild={}", guildId)

        return wowAuditClient.fetchAttendance(guildConfig.wowauditApiKeyEncrypted)
            .map { body -> parseAndSyncAttendance(body, guildId) }
            .doOnSuccess { result ->
                logger.info(
                    "WoWAudit attendance sync completed for guild={}: created={}, updated={}, skipped={}",
                    guildId,
                    result.created,
                    result.updated,
                    result.skipped,
                )
            }
            .doOnError { ex ->
                logger.error("WoWAudit attendance sync failed for guild={}: {}", guildId, ex.message, ex)
            }
    }

    private fun parseAndSyncAttendance(
        body: String,
        guildId: String,
    ): WoWAuditSyncResult {
        var created = 0
        var updated = 0
        var skipped = 0

        try {
            val node = objectMapper.readTree(body)

            // WoWAudit attendance response structure can vary
            // It may have a "characters" array or be a direct array
            val charactersNode =
                when {
                    node.has("characters") -> node.get("characters")
                    node.isArray -> node
                    else -> {
                        logger.warn("WoWAudit attendance response has unexpected structure")
                        return WoWAuditSyncResult(0, 0, 0, "Unexpected response structure")
                    }
                }

            if (!charactersNode.isArray) {
                logger.warn("WoWAudit attendance characters is not an array")
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
                    if (characterName.isBlank()) {
                        skipped++
                        continue
                    }

                    val characterRealm = element.path("realm").asText(null)
                    val characterClass = element.path("class").asText(null)
                    val characterRole = element.path("role").asText(null)
                    val characterRegion = element.path("region").asText(null)
                    // Lookup raider to get internal ID
                    val wowauditId = element.path("id").asLongOrNull()
                    var raider = wowauditId?.let { raiderEntityRepository.findByWowauditId(it) }

                    if (raider == null && characterName.isNotBlank()) {
                        // Fallback to name/realm lookup
                        val realmToSearch = characterRealm ?: "" // Fallback logic could be better but sticking to current scope
                        raider = raiderEntityRepository.findByCharacterNameAndRealmNormalized(characterName, realmToSearch)
                    }

                    // Extract attendance stats
                    val instance = null // Overall stats usually don't have instance/encounter context in this endpoint
                    val encounter = null
                    val startDate = null
                    val endDate = null

                    val attendedAmountOfRaids = element.path("attended_raids").asIntOrNull()
                    val totalAmountOfRaids = element.path("total_raids").asIntOrNull()
                    val attendedPercentage = element.path("attendance_percentage").asDoubleOrNull() // e.g. 95.5

                    // These might be specific to recent raids or period
                    val selectedAmountOfEncounters = element.path("encounters_attended").asIntOrNull()
                    val totalAmountOfEncounters = element.path("total_encounters").asIntOrNull()
                    val selectedPercentage = element.path("encounters_percentage").asDoubleOrNull()

                    val internalRaiderId = raider?.id

                    if (internalRaiderId == null) {
                        // Optional: Create placeholder raider? No, Roster Sync should run first.
                        logger.debug("Skipping attendance for unknown raider: {} (ID: {})", characterName, wowauditId)
                        skipped++
                        continue
                    }

                    // Check duplicate? (Optional, repository usually handles upsert or we perform check)
                    // The repo is PagingAndSortingRepository, usually save() updates if ID is present.
                    // But here we are creating NEW AttendanceStatEntity objects without ID.
                    // We should probably check if it exists for this character+period/date.
                    // But for now, let's fix the linkage ID which is the primary reported issue.

                    val entity =
                        AttendanceStatEntity(
                            instance = instance,
                            encounter = encounter,
                            startDate = startDate,
                            endDate = endDate,
                            characterId = internalRaiderId, // Use Internal ID
                            characterName = characterName,
                            characterRealm = characterRealm,
                            characterClass = characterClass,
                            characterRole = characterRole,
                            characterRegion = characterRegion,
                            attendedAmountOfRaids = attendedAmountOfRaids,
                            totalAmountOfRaids = totalAmountOfRaids,
                            attendedPercentage = attendedPercentage,
                            selectedAmountOfEncounters = selectedAmountOfEncounters,
                            totalAmountOfEncounters = totalAmountOfEncounters,
                            selectedPercentage = selectedPercentage,
                            teamId = teamId,
                            seasonId = seasonId,
                            periodId = periodId,
                            syncedAt = now,
                        )

                    attendanceStatRepository.save(entity)
                    created++
                } catch (ex: Exception) {
                    val name = element.path("name").asText("unknown")
                    logger.warn("Failed to process attendance for {}: {}", name, ex.message)
                    skipped++
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to parse WoWAudit attendance response: {}", ex.message, ex)
            return WoWAuditSyncResult(created, updated, skipped, ex.message)
        }

        return WoWAuditSyncResult(created, updated, skipped, null)
    }

    private fun parseLocalDate(text: String?): LocalDate? {
        if (text.isNullOrBlank()) return null
        return try {
            LocalDate.parse(text)
        } catch (ex: Exception) {
            try {
                LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE)
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

    private fun JsonNode.asLongOrNull(): Long? {
        return if (this.isNumber) this.asLong() else null
    }
}
