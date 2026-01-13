package com.edgerush.lootman.domain.flps.repository

import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

/**
 * Repository interface for raider performance data (Warcraft Logs integration).
 *
 * Provides access to aggregated performance metrics used for MAS calculation.
 */
interface RaiderPerformanceRepository {
    /**
     * Finds performance data for a raider within a time period.
     *
     * @param raiderId The raider identifier
     * @param guildId The guild identifier
     * @param startDate Start of the period to analyze
     * @param endDate End of the period to analyze
     * @return RaiderPerformanceData if logs exist, null otherwise
     */
    fun findByRaiderAndPeriod(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): RaiderPerformanceData?

    /**
     * Finds performance data for a character by name and realm.
     *
     * @param characterName The character name
     * @param characterRealm The character realm
     * @param guildId The guild identifier
     * @param startDate Start of the period to analyze
     * @param endDate End of the period to analyze
     * @return RaiderPerformanceData if logs exist, null otherwise
     */
    fun findByCharacterAndPeriod(
        characterName: String,
        characterRealm: String,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): RaiderPerformanceData?

    /**
     * Finds all performance data for a guild within a period.
     *
     * @param guildId The guild identifier
     * @param startDate Start of the period to analyze
     * @param endDate End of the period to analyze
     * @return List of performance data for all raiders in the guild
     */
    fun findAllByGuildAndPeriod(
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): List<RaiderPerformanceData>
}
