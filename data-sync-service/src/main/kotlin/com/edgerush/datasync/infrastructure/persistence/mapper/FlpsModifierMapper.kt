package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.domain.flps.repository.*
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Mapper for converting between FLPS domain models and database entities.
 * Handles the transformation of guild-specific FLPS configuration.
 */
@Component
class FlpsModifierMapper {

    /**
     * Maps a list of FlpsGuildModifierEntity to FlpsConfiguration domain model.
     * Uses default values for any missing modifiers.
     *
     * @param entities List of modifier entities from the database
     * @return FlpsConfiguration domain model
     */
    fun toDomain(entities: List<FlpsGuildModifierEntity>): FlpsConfiguration {
        if (entities.isEmpty()) {
            return FlpsConfiguration.default()
        }

        val modifierMap = entities.groupBy { it.category }
            .mapValues { (_, list) ->
                list.associate { it.modifierKey to it.modifierValue.toDouble() }
            }

        val defaults = FlpsConfiguration.default()

        return FlpsConfiguration(
            rmsWeights = RmsWeights(
                attendance = modifierMap["rms"]?.get("attendance_weight")
                    ?: defaults.rmsWeights.attendance,
                mechanical = modifierMap["rms"]?.get("mechanical_weight")
                    ?: defaults.rmsWeights.mechanical,
                preparation = modifierMap["rms"]?.get("preparation_weight")
                    ?: defaults.rmsWeights.preparation
            ),
            ipiWeights = IpiWeights(
                upgradeValue = modifierMap["ipi"]?.get("upgrade_value_weight")
                    ?: defaults.ipiWeights.upgradeValue,
                tierBonus = modifierMap["ipi"]?.get("tier_bonus_weight")
                    ?: defaults.ipiWeights.tierBonus,
                roleMultiplier = modifierMap["ipi"]?.get("role_multiplier_weight")
                    ?: defaults.ipiWeights.roleMultiplier
            ),
            thresholds = FlpsThresholds(
                eligibilityAttendance = modifierMap["threshold"]?.get("eligibility_attendance")
                    ?: defaults.thresholds.eligibilityAttendance,
                eligibilityActivity = modifierMap["threshold"]?.get("eligibility_activity")
                    ?: defaults.thresholds.eligibilityActivity
            ),
            roleMultipliers = RoleMultipliers(
                tank = modifierMap["role"]?.get("tank_multiplier")
                    ?: defaults.roleMultipliers.tank,
                healer = modifierMap["role"]?.get("healer_multiplier")
                    ?: defaults.roleMultipliers.healer,
                dps = modifierMap["role"]?.get("dps_multiplier")
                    ?: defaults.roleMultipliers.dps
            ),
            recencyPenalties = RecencyPenalties(
                tierA = modifierMap["recency"]?.get("tier_a_penalty")
                    ?: defaults.recencyPenalties.tierA,
                tierB = modifierMap["recency"]?.get("tier_b_penalty")
                    ?: defaults.recencyPenalties.tierB,
                tierC = modifierMap["recency"]?.get("tier_c_penalty")
                    ?: defaults.recencyPenalties.tierC,
                recoveryRate = modifierMap["recency"]?.get("recovery_rate")
                    ?: defaults.recencyPenalties.recoveryRate
            )
        )
    }

    /**
     * Maps FlpsConfiguration domain model to a list of FlpsGuildModifierEntity.
     *
     * @param guildId The guild identifier
     * @param configuration The FLPS configuration to map
     * @return List of modifier entities ready to be persisted
     */
    fun toEntities(guildId: String, configuration: FlpsConfiguration): List<FlpsGuildModifierEntity> {
        return listOf(
            // RMS weights
            createEntity(guildId, "rms", "attendance_weight", configuration.rmsWeights.attendance),
            createEntity(guildId, "rms", "mechanical_weight", configuration.rmsWeights.mechanical),
            createEntity(guildId, "rms", "preparation_weight", configuration.rmsWeights.preparation),

            // IPI weights
            createEntity(guildId, "ipi", "upgrade_value_weight", configuration.ipiWeights.upgradeValue),
            createEntity(guildId, "ipi", "tier_bonus_weight", configuration.ipiWeights.tierBonus),
            createEntity(guildId, "ipi", "role_multiplier_weight", configuration.ipiWeights.roleMultiplier),

            // Thresholds
            createEntity(guildId, "threshold", "eligibility_attendance", configuration.thresholds.eligibilityAttendance),
            createEntity(guildId, "threshold", "eligibility_activity", configuration.thresholds.eligibilityActivity),

            // Role multipliers
            createEntity(guildId, "role", "tank_multiplier", configuration.roleMultipliers.tank),
            createEntity(guildId, "role", "healer_multiplier", configuration.roleMultipliers.healer),
            createEntity(guildId, "role", "dps_multiplier", configuration.roleMultipliers.dps),

            // Recency penalties
            createEntity(guildId, "recency", "tier_a_penalty", configuration.recencyPenalties.tierA),
            createEntity(guildId, "recency", "tier_b_penalty", configuration.recencyPenalties.tierB),
            createEntity(guildId, "recency", "tier_c_penalty", configuration.recencyPenalties.tierC),
            createEntity(guildId, "recency", "recovery_rate", configuration.recencyPenalties.recoveryRate)
        )
    }

    private fun createEntity(
        guildId: String,
        category: String,
        key: String,
        value: Double
    ): FlpsGuildModifierEntity {
        return FlpsGuildModifierEntity(
            id = null,
            guildId = guildId,
            category = category,
            modifierKey = key,
            modifierValue = BigDecimal.valueOf(value),
            description = null
        )
    }
}
