package com.edgerush.datasync.service

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.repository.FlpsDefaultModifierRepository
import com.edgerush.datasync.repository.FlpsGuildModifierRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class FlpsModifierService(
    private val defaultModifierRepository: FlpsDefaultModifierRepository,
    private val guildModifierRepository: FlpsGuildModifierRepository,
) {
    /**
     * Get modifier value for a specific guild, falling back to default if not overridden
     */
    fun getModifier(
        guildId: String,
        category: String,
        key: String,
    ): Double {
        // Try guild-specific override first
        val guildModifier = guildModifierRepository.findByGuildIdAndCategoryAndModifierKey(guildId, category, key)
        if (guildModifier != null) {
            return guildModifier.modifierValue.toDouble()
        }

        // Fall back to system default
        val defaultModifier = defaultModifierRepository.findByCategoryAndModifierKey(category, key)
        return defaultModifier?.modifierValue?.toDouble() ?: 0.0
    }

    /**
     * Get all modifiers for a guild (with defaults filled in where no override exists)
     */
    fun getGuildModifiers(guildId: String): FlpsGuildConfig {
        return FlpsGuildConfig(
            guildId = guildId,
            rmsWeights =
                RmsWeights(
                    attendance = getModifier(guildId, "rms", "attendance_weight"),
                    mechanical = getModifier(guildId, "rms", "mechanical_weight"),
                    preparation = getModifier(guildId, "rms", "preparation_weight"),
                ),
            ipiWeights =
                IpiWeights(
                    upgradeValue = getModifier(guildId, "ipi", "upgrade_value_weight"),
                    tierBonus = getModifier(guildId, "ipi", "tier_bonus_weight"),
                    roleMultiplier = getModifier(guildId, "ipi", "role_multiplier_weight"),
                ),
            roleMultipliers =
                RoleMultipliers(
                    tank = getModifier(guildId, "role", "tank_multiplier"),
                    healer = getModifier(guildId, "role", "healer_multiplier"),
                    dps = getModifier(guildId, "role", "dps_multiplier"),
                ),
            thresholds =
                Thresholds(
                    eligibilityAttendance = getModifier(guildId, "threshold", "eligibility_attendance"),
                    eligibilityActivity = getModifier(guildId, "threshold", "eligibility_activity"),
                    recencyDecayDays = getModifier(guildId, "threshold", "recency_decay_days").toLong(),
                ),
            limits =
                Limits(
                    maxAttendanceBonus = getModifier(guildId, "limit", "max_attendance_bonus"),
                    minMechanicalScore = getModifier(guildId, "limit", "min_mechanical_score"),
                    maxPreparationScore = getModifier(guildId, "limit", "max_preparation_score"),
                ),
        )
    }

    /**
     * Set guild-specific modifier override
     */
    fun setGuildModifier(
        guildId: String,
        category: String,
        key: String,
        value: Double,
        description: String? = null,
    ) {
        val existing = guildModifierRepository.findByGuildIdAndCategoryAndModifierKey(guildId, category, key)

        if (existing != null) {
            // Update existing
            val updated =
                existing.copy(
                    modifierValue = BigDecimal.valueOf(value),
                    description = description ?: existing.description,
                )
            guildModifierRepository.save(updated)
        } else {
            // Create new override
            val newModifier =
                com.edgerush.datasync.entity.FlpsGuildModifierEntity(
                    guildId = guildId,
                    category = category,
                    modifierKey = key,
                    modifierValue = BigDecimal.valueOf(value),
                    description = description,
                )
            guildModifierRepository.save(newModifier)
        }
    }

    /**
     * Remove guild-specific override (fall back to default)
     */
    fun removeGuildModifier(
        guildId: String,
        category: String,
        key: String,
    ) {
        val existing = guildModifierRepository.findByGuildIdAndCategoryAndModifierKey(guildId, category, key)
        if (existing != null) {
            guildModifierRepository.delete(existing)
        }
    }
}

data class FlpsGuildConfig(
    val guildId: String,
    val rmsWeights: RmsWeights,
    val ipiWeights: IpiWeights,
    val roleMultipliers: RoleMultipliers,
    val thresholds: Thresholds,
    val limits: Limits,
)

data class RoleMultipliers(
    val tank: Double, // Tank role multiplier
    val healer: Double, // Healer role multiplier
    val dps: Double, // DPS role multiplier
)

data class Thresholds(
    val eligibilityAttendance: Double, // Minimum attendance for eligibility
    val eligibilityActivity: Double, // Minimum activity for eligibility
    val recencyDecayDays: Long, // Days for RDF calculation
)

data class Limits(
    val maxAttendanceBonus: Double, // Maximum attendance score
    val minMechanicalScore: Double, // Minimum mechanical score
    val maxPreparationScore: Double, // Maximum preparation score
)
