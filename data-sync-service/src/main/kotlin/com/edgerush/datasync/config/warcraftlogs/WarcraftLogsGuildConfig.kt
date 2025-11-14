package com.edgerush.datasync.config.warcraftlogs

/**
 * Guild-specific configuration for Warcraft Logs integration
 * Stored as JSON in warcraft_logs_config.config_json
 */
data class WarcraftLogsGuildConfig(
    val guildId: String,
    val enabled: Boolean = true,
    val guildName: String,
    val realm: String,
    val region: String,
    val clientId: String? = null, // Guild-specific credentials (optional)
    val clientSecret: String? = null,
    // Sync configuration
    val syncIntervalHours: Int = 6,
    val syncTimeWindowDays: Int = 30,
    val includedDifficulties: List<String> = listOf("Mythic", "Heroic"),
    // MAS calculation configuration
    val dpaWeight: Double = 0.25,
    val adtWeight: Double = 0.25,
    val criticalThreshold: Double = 1.5,
    val fallbackMAS: Double = 0.0,
    val fallbackDPA: Double = 0.5,
    val fallbackADT: Double = 10.0,
    // Time weighting configuration
    val recentPerformanceWeightMultiplier: Double = 2.0,
    val recentPerformanceDays: Int = 14,
    // Spec average calculation
    val specAveragePercentile: Int = 50,
    val minimumSampleSize: Int = 5,
    // Cache configuration
    val masCacheTTLMinutes: Int = 60,
    // Character mapping
    val characterNameMappings: Map<String, String> = emptyMap(), // WoWAudit name -> WCL name
)
