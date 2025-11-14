package com.edgerush.datasync.config.raidbots

data class RaidbotsGuildConfig(
    val guildId: String,
    val enabled: Boolean = true,
    val apiKey: String? = null, // Guild-specific API key (optional)
    // Raiders submit Droptimizer URLs manually
    // System parses the public report pages
    val requireDroptimizerUrls: Boolean = true,
    // UV calculation
    val uvNormalizationPercentile: Int = 95,
    val recentSimWeightMultiplier: Double = 2.0,
    val recentSimDays: Int = 7,
    val staleSimThresholdDays: Int = 30,
    // Cache configuration
    val uvCacheTTLHours: Int = 24,
)
