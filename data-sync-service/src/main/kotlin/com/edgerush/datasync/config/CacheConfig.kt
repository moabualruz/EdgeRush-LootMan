package com.edgerush.datasync.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Cache configuration using Caffeine for high-performance caching.
 *
 * Cache names and their purposes:
 * - flps-leaderboard: Guild FLPS leaderboard (changes rarely, expensive to compute)
 * - flps-character: Individual character FLPS scores
 * - raider-profile: Full raider profile data
 * - guild-config: Guild configuration (rarely changes)
 * - loot-history: Recent loot awards (paginated)
 * - raid-roster: Raid signups and roster
 */
@Configuration
@EnableCaching
class CacheConfig {

    companion object {
        const val FLPS_LEADERBOARD = "flps-leaderboard"
        const val FLPS_CHARACTER = "flps-character"
        const val RAIDER_PROFILE = "raider-profile"
        const val GUILD_CONFIG = "guild-config"
        const val LOOT_HISTORY = "loot-history"
        const val RAID_ROSTER = "raid-roster"
        const val WARCRAFT_LOGS = "warcraft-logs"
        const val SIMULATION_RESULTS = "simulation-results"
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()

        // Configure different caches with appropriate settings
        cacheManager.registerCustomCache(
            FLPS_LEADERBOARD,
            Caffeine.newBuilder()
                .maximumSize(100) // One per guild
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            FLPS_CHARACTER,
            Caffeine.newBuilder()
                .maximumSize(1000) // Individual character scores
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            RAIDER_PROFILE,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            GUILD_CONFIG,
            Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofHours(1))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            LOOT_HISTORY,
            Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofMinutes(2))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            RAID_ROSTER,
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(1))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            WARCRAFT_LOGS,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build(),
        )

        cacheManager.registerCustomCache(
            SIMULATION_RESULTS,
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofHours(2))
                .recordStats()
                .build(),
        )

        return cacheManager
    }
}
