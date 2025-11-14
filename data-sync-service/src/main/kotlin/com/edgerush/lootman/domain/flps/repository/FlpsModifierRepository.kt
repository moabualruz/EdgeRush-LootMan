package com.edgerush.lootman.domain.flps.repository

import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for FLPS modifiers.
 *
 * This interface defines the contract for accessing guild-specific FLPS configuration.
 * Implementation will be in the infrastructure layer.
 */
interface FlpsModifierRepository {
    /**
     * Finds FLPS modifiers for a specific guild.
     *
     * @param guildId The guild identifier
     * @return FlpsModifiers for the guild, or default modifiers if not found
     */
    fun findByGuildId(guildId: GuildId): FlpsModifiers
}

/**
 * Data class representing FLPS modifiers for a guild.
 */
data class FlpsModifiers(
    val guildId: GuildId,
    val rmsWeights: RmsWeights = RmsWeights(),
    val ipiWeights: IpiWeights = IpiWeights(),
    val roleMultipliers: RoleMultipliers = RoleMultipliers(),
    val thresholds: FlpsThresholds = FlpsThresholds(),
)

/**
 * Weights for RMS components.
 */
data class RmsWeights(
    val attendance: Double = 0.4,
    val mechanical: Double = 0.4,
    val preparation: Double = 0.2,
)

/**
 * Weights for IPI components.
 */
data class IpiWeights(
    val upgradeValue: Double = 0.45,
    val tierBonus: Double = 0.35,
    val roleMultiplier: Double = 0.20,
)

/**
 * Role multipliers for different roles.
 */
data class RoleMultipliers(
    val dps: Double = 1.0,
    val tank: Double = 0.8,
    val healer: Double = 0.7,
)

/**
 * Thresholds for FLPS eligibility.
 */
data class FlpsThresholds(
    val eligibilityAttendance: Double = 0.8,
    val eligibilityActivity: Double = 0.0,
)
