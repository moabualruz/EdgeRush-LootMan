package com.edgerush.lootman.domain.guild.model

/**
 * Configuration for how attendance is aggregated within a guild.
 *
 * This allows guilds to configure whether attendance should be tracked
 * per-character or aggregated across all characters on the same account.
 */
data class AttendanceAggregationConfig(
    /**
     * How attendance is aggregated.
     */
    val mode: AggregationMode = AggregationMode.CHARACTER,

    /**
     * Scope of attendance aggregation.
     */
    val scope: AggregationScope = AggregationScope.GUILD,
) {
    companion object {
        /**
         * Default configuration: track each character separately within the guild.
         */
        val DEFAULT = AttendanceAggregationConfig()

        /**
         * Configuration for account-level aggregation within the guild.
         * All characters on the same account contribute to a single attendance score.
         */
        val ACCOUNT_BASED = AttendanceAggregationConfig(
            mode = AggregationMode.ACCOUNT,
            scope = AggregationScope.GUILD,
        )
    }
}

/**
 * How attendance is aggregated.
 */
enum class AggregationMode {
    /**
     * Each character's attendance is tracked separately.
     * This is the traditional approach where alt characters have their own attendance.
     */
    CHARACTER,

    /**
     * All characters on the same account are aggregated together.
     * If a player attends on any character, it counts for all their characters.
     */
    ACCOUNT,
    ;

    companion object {
        fun fromString(value: String): AggregationMode? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Scope of attendance aggregation.
 */
enum class AggregationScope {
    /**
     * Only raids in the current guild count toward attendance.
     */
    GUILD,

    /**
     * Raids across all guilds count toward attendance.
     * Useful for players who raid in multiple guilds with the same account.
     */
    GLOBAL,
    ;

    companion object {
        fun fromString(value: String): AggregationScope? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
