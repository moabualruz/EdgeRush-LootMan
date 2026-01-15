package com.edgerush.lootman.domain.guild.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * Guild aggregate root representing a World of Warcraft guild.
 *
 * A guild is the organizational unit that contains raiders and
 * manages loot distribution using the FLPS system.
 */
data class Guild(
    val id: GuildId,
    val name: String,
    val description: String?,
    val realm: String?,
    val region: Region,
    val settings: GuildSettings,
    val syncStatus: SyncStatus,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(name.isNotBlank()) { "Guild name cannot be blank" }
    }

    /**
     * Checks if the guild is currently syncing data.
     */
    fun isSyncing(): Boolean = syncStatus == SyncStatus.IN_PROGRESS

    /**
     * Checks if sync is enabled for this guild.
     */
    fun canSync(): Boolean = isActive && settings.syncEnabled
}

/**
 * World of Warcraft region.
 */
enum class Region {
    US,
    EU,
    KR,
    TW,
    CN,
    ;

    companion object {
        fun fromString(value: String): Region? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Guild settings for FLPS and sync configuration.
 */
data class GuildSettings(
    val syncEnabled: Boolean = true,
    val syncCronExpression: String = "0 0 4 * * *",
    val syncRunOnStartup: Boolean = false,
    val timezone: String = "UTC",
    val benchmarkMode: BenchmarkMode = BenchmarkMode.THEORETICAL,
    val customBenchmarkRms: Double? = null,
    val customBenchmarkIpi: Double? = null,
) {
    companion object {
        fun default(): GuildSettings = GuildSettings()
    }
}

/**
 * How to calculate perfect score for FLPS benchmarking.
 */
enum class BenchmarkMode {
    THEORETICAL, // Use mathematical perfect scores (1.0 for all components)
    TOP_PERFORMER, // Use the highest actual scores achieved by guild members
    CUSTOM, // Use manually set benchmark values
    ;

    companion object {
        fun fromString(value: String): BenchmarkMode? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Status of guild data synchronization.
 */
enum class SyncStatus {
    NEVER_RUN,
    SUCCESS,
    FAILED,
    IN_PROGRESS,
    ;

    companion object {
        fun fromString(value: String): SyncStatus? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
