package com.edgerush.datasync.domain.flps.repository

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights

/**
 * Repository interface for FLPS modifiers and configuration.
 * This interface defines the contract for accessing guild-specific FLPS settings
 * without coupling the domain layer to infrastructure concerns.
 */
interface FlpsModifierRepository {

    /**
     * Retrieves FLPS configuration for a specific guild.
     *
     * @param guildId The guild identifier
     * @return FlpsConfiguration for the guild, or default configuration if not found
     */
    fun findByGuildId(guildId: String): FlpsConfiguration

    /**
     * Saves FLPS configuration for a guild.
     *
     * @param guildId The guild identifier
     * @param configuration The configuration to save
     */
    fun save(guildId: String, configuration: FlpsConfiguration)
}

/**
 * Domain model representing FLPS configuration for a guild.
 * This encapsulates all customizable weights and thresholds.
 */
data class FlpsConfiguration(
    val rmsWeights: RmsWeights,
    val ipiWeights: IpiWeights,
    val thresholds: FlpsThresholds,
    val roleMultipliers: RoleMultipliers,
    val recencyPenalties: RecencyPenalties
) {
    companion object {
        /**
         * Returns default FLPS configuration.
         */
        fun default(): FlpsConfiguration = FlpsConfiguration(
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds.default(),
            roleMultipliers = RoleMultipliers.default(),
            recencyPenalties = RecencyPenalties.default()
        )
    }
}

/**
 * Eligibility thresholds for FLPS calculations.
 */
data class FlpsThresholds(
    val eligibilityAttendance: Double,
    val eligibilityActivity: Double
) {
    companion object {
        fun default(): FlpsThresholds = FlpsThresholds(
            eligibilityAttendance = 0.8,
            eligibilityActivity = 0.0
        )
    }
}

/**
 * Role-specific multipliers for IPI calculations.
 */
data class RoleMultipliers(
    val tank: Double,
    val healer: Double,
    val dps: Double
) {
    companion object {
        fun default(): RoleMultipliers = RoleMultipliers(
            tank = 0.8,
            healer = 0.7,
            dps = 1.0
        )
    }
}

/**
 * Recency penalty configuration for RDF calculations.
 */
data class RecencyPenalties(
    val tierA: Double,
    val tierB: Double,
    val tierC: Double,
    val recoveryRate: Double
) {
    companion object {
        fun default(): RecencyPenalties = RecencyPenalties(
            tierA = 0.8,
            tierB = 0.9,
            tierC = 1.0,
            recoveryRate = 0.1
        )
    }
}
