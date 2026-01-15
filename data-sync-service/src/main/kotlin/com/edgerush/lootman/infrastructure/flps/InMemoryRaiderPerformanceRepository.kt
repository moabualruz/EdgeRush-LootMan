package com.edgerush.lootman.infrastructure.flps

import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of RaiderPerformanceRepository.
 *
 * Stores raider performance data keyed by guild, raider, and period.
 * Useful for testing and development without database dependency.
 */
class InMemoryRaiderPerformanceRepository : RaiderPerformanceRepository {
    // Storage keyed by composite key: guildId-raiderId-periodStart-periodEnd
    private val storage = ConcurrentHashMap<String, RaiderPerformanceData>()

    override fun findByRaiderAndPeriod(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): RaiderPerformanceData? {
        val key = buildKey(guildId, raiderId, startDate, endDate)
        return storage[key]
    }

    override fun findByCharacterAndPeriod(
        characterName: String,
        characterRealm: String,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): RaiderPerformanceData? {
        return storage.values.find { data ->
            data.characterName == characterName &&
                data.characterRealm == characterRealm &&
                matchesPeriod(data, startDate, endDate) &&
                matchesGuild(data, guildId)
        }
    }

    override fun findAllByGuildAndPeriod(
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): List<RaiderPerformanceData> {
        return storage.entries
            .filter { (key, data) ->
                key.startsWith("${guildId.value}-") &&
                    matchesPeriod(data, startDate, endDate)
            }
            .map { it.value }
            .sortedBy { it.characterName }
    }

    /**
     * Saves performance data to the repository.
     *
     * @param guildId The guild identifier
     * @param data The performance data to save
     */
    fun save(
        guildId: GuildId,
        data: RaiderPerformanceData,
    ) {
        val key = buildKey(guildId, data.raiderId, data.periodStart, data.periodEnd)
        storage[key] = data
    }

    /**
     * Clears all stored data (for testing purposes).
     */
    fun clear() {
        storage.clear()
    }

    private fun buildKey(
        guildId: GuildId,
        raiderId: RaiderId,
        periodStart: Instant,
        periodEnd: Instant,
    ): String {
        return "${guildId.value}-${raiderId.value}-${periodStart.epochSecond}-${periodEnd.epochSecond}"
    }

    private fun matchesPeriod(
        data: RaiderPerformanceData,
        startDate: Instant,
        endDate: Instant,
    ): Boolean {
        return data.periodStart == startDate && data.periodEnd == endDate
    }

    private fun matchesGuild(
        data: RaiderPerformanceData,
        guildId: GuildId,
    ): Boolean {
        // Find the entry in storage to check if it was saved with this guild
        return storage.entries.any { (key, value) ->
            key.startsWith("${guildId.value}-") && value == data
        }
    }
}
