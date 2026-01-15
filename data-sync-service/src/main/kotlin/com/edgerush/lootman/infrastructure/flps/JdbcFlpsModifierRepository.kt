package com.edgerush.lootman.infrastructure.flps

import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.repository.FlpsThresholds
import com.edgerush.lootman.domain.flps.repository.IpiWeights
import com.edgerush.lootman.domain.flps.repository.RmsWeights
import com.edgerush.lootman.domain.flps.repository.RoleMultipliers
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of FlpsModifierRepository.
 *
 * Reads FLPS modifiers from the database tables:
 * - flps_default_modifiers: System-wide default values
 * - flps_guild_modifiers: Guild-specific overrides
 *
 * Guild overrides take precedence over defaults.
 */
@Repository
@Primary
class JdbcFlpsModifierRepository(
    private val jdbcTemplate: JdbcTemplate,
) : FlpsModifierRepository {
    override fun findByGuildId(guildId: GuildId): FlpsModifiers {
        // Load default modifiers
        val defaults = loadDefaultModifiers()

        // Load guild-specific overrides
        val overrides = loadGuildOverrides(guildId)

        // Merge overrides with defaults
        val merged = mergeModifiers(defaults, overrides)

        // Build FlpsModifiers from merged values
        return buildFlpsModifiers(guildId, merged)
    }

    private fun loadDefaultModifiers(): Map<String, Double> {
        val sql =
            """
            SELECT category, modifier_key, modifier_value
            FROM flps_default_modifiers
            """.trimIndent()

        val rows = jdbcTemplate.query(sql, modifierRowMapper)
        return rows.associate { "${it.category}.${it.modifierKey}" to it.modifierValue }
    }

    private fun loadGuildOverrides(guildId: GuildId): Map<String, Double> {
        val sql =
            """
            SELECT category, modifier_key, modifier_value
            FROM flps_guild_modifiers
            WHERE guild_id = ?
            """.trimIndent()

        val rows = jdbcTemplate.query(sql, modifierRowMapper, guildId.value)
        return rows.associate { "${it.category}.${it.modifierKey}" to it.modifierValue }
    }

    private fun mergeModifiers(
        defaults: Map<String, Double>,
        overrides: Map<String, Double>,
    ): Map<String, Double> {
        return defaults + overrides // Overrides take precedence
    }

    private fun buildFlpsModifiers(
        guildId: GuildId,
        modifiers: Map<String, Double>,
    ): FlpsModifiers {
        return FlpsModifiers(
            guildId = guildId,
            rmsWeights =
                RmsWeights(
                    attendance = modifiers["rms.attendance_weight"] ?: RmsWeights().attendance,
                    mechanical = modifiers["rms.mechanical_weight"] ?: RmsWeights().mechanical,
                    preparation = modifiers["rms.preparation_weight"] ?: RmsWeights().preparation,
                ),
            ipiWeights =
                IpiWeights(
                    upgradeValue = modifiers["ipi.upgrade_value_weight"] ?: IpiWeights().upgradeValue,
                    tierBonus = modifiers["ipi.tier_bonus_weight"] ?: IpiWeights().tierBonus,
                    roleMultiplier = modifiers["ipi.role_multiplier_weight"] ?: IpiWeights().roleMultiplier,
                ),
            roleMultipliers =
                RoleMultipliers(
                    tank = modifiers["role.tank_multiplier"] ?: RoleMultipliers().tank,
                    healer = modifiers["role.healer_multiplier"] ?: RoleMultipliers().healer,
                    dps = modifiers["role.dps_multiplier"] ?: RoleMultipliers().dps,
                ),
            thresholds =
                FlpsThresholds(
                    eligibilityAttendance = modifiers["threshold.eligibility_attendance"] ?: FlpsThresholds().eligibilityAttendance,
                    eligibilityActivity = modifiers["threshold.eligibility_activity"] ?: FlpsThresholds().eligibilityActivity,
                ),
        )
    }

    private val modifierRowMapper =
        RowMapper { rs, _ ->
            ModifierRow(
                category = rs.getString("category"),
                modifierKey = rs.getString("modifier_key"),
                modifierValue = rs.getDouble("modifier_value"),
            )
        }

    /**
     * Internal data class to represent a row from the modifiers tables.
     */
    private data class ModifierRow(
        val category: String,
        val modifierKey: String,
        val modifierValue: Double,
    )
}
