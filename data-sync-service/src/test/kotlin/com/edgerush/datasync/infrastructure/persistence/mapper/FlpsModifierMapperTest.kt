package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.domain.flps.repository.*
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Unit tests for FlpsModifierMapper.
 * Tests the mapping between domain models and database entities.
 */
class FlpsModifierMapperTest {

    private val mapper = FlpsModifierMapper()

    @Test
    fun `should map entities to configuration with all values present`() {
        // Given
        val guildId = "test-guild"
        val entities = listOf(
            createEntity(guildId, "rms", "attendance_weight", 0.5),
            createEntity(guildId, "rms", "mechanical_weight", 0.3),
            createEntity(guildId, "rms", "preparation_weight", 0.2),
            createEntity(guildId, "ipi", "upgrade_value_weight", 0.4),
            createEntity(guildId, "ipi", "tier_bonus_weight", 0.4),
            createEntity(guildId, "ipi", "role_multiplier_weight", 0.2),
            createEntity(guildId, "role", "tank_multiplier", 1.3),
            createEntity(guildId, "role", "healer_multiplier", 1.2),
            createEntity(guildId, "role", "dps_multiplier", 1.0),
            createEntity(guildId, "threshold", "eligibility_attendance", 0.85),
            createEntity(guildId, "threshold", "eligibility_activity", 0.05),
            createEntity(guildId, "recency", "tier_a_penalty", 0.7),
            createEntity(guildId, "recency", "tier_b_penalty", 0.85),
            createEntity(guildId, "recency", "tier_c_penalty", 0.95),
            createEntity(guildId, "recency", "recovery_rate", 0.15)
        )

        // When
        val result = mapper.toDomain(entities)

        // Then
        assertNotNull(result)
        
        // RMS weights
        assertEquals(0.5, result.rmsWeights.attendance)
        assertEquals(0.3, result.rmsWeights.mechanical)
        assertEquals(0.2, result.rmsWeights.preparation)
        
        // IPI weights
        assertEquals(0.4, result.ipiWeights.upgradeValue)
        assertEquals(0.4, result.ipiWeights.tierBonus)
        assertEquals(0.2, result.ipiWeights.roleMultiplier)
        
        // Role multipliers
        assertEquals(1.3, result.roleMultipliers.tank)
        assertEquals(1.2, result.roleMultipliers.healer)
        assertEquals(1.0, result.roleMultipliers.dps)
        
        // Thresholds
        assertEquals(0.85, result.thresholds.eligibilityAttendance)
        assertEquals(0.05, result.thresholds.eligibilityActivity)
        
        // Recency penalties
        assertEquals(0.7, result.recencyPenalties.tierA)
        assertEquals(0.85, result.recencyPenalties.tierB)
        assertEquals(0.95, result.recencyPenalties.tierC)
        assertEquals(0.15, result.recencyPenalties.recoveryRate)
    }

    @Test
    fun `should use default values when entities are empty`() {
        // Given
        val entities = emptyList<FlpsGuildModifierEntity>()

        // When
        val result = mapper.toDomain(entities)

        // Then
        assertEquals(FlpsConfiguration.default(), result)
    }

    @Test
    fun `should use default values for missing categories`() {
        // Given
        val guildId = "test-guild"
        val entities = listOf(
            createEntity(guildId, "rms", "attendance_weight", 0.6)
            // Only one value, rest should be defaults
        )

        // When
        val result = mapper.toDomain(entities)

        // Then
        val defaults = FlpsConfiguration.default()
        assertEquals(0.6, result.rmsWeights.attendance) // Custom value
        assertEquals(defaults.rmsWeights.mechanical, result.rmsWeights.mechanical)
        assertEquals(defaults.rmsWeights.preparation, result.rmsWeights.preparation)
        assertEquals(defaults.ipiWeights, result.ipiWeights)
        assertEquals(defaults.roleMultipliers, result.roleMultipliers)
        assertEquals(defaults.thresholds, result.thresholds)
        assertEquals(defaults.recencyPenalties, result.recencyPenalties)
    }

    @Test
    fun `should map configuration to entities`() {
        // Given
        val guildId = "test-guild"
        val configuration = FlpsConfiguration(
            rmsWeights = RmsWeights(0.5, 0.3, 0.2),
            ipiWeights = IpiWeights(0.4, 0.4, 0.2),
            thresholds = FlpsThresholds(0.85, 0.05),
            roleMultipliers = RoleMultipliers(1.3, 1.2, 1.0),
            recencyPenalties = RecencyPenalties(0.7, 0.85, 0.95, 0.15)
        )

        // When
        val result = mapper.toEntities(guildId, configuration)

        // Then
        assertEquals(15, result.size) // 3 RMS + 3 IPI + 2 thresholds + 3 roles + 4 recency
        
        // Verify some key mappings
        val attendanceWeight = result.find { it.category == "rms" && it.modifierKey == "attendance_weight" }
        assertNotNull(attendanceWeight)
        assertEquals(guildId, attendanceWeight?.guildId)
        assertEquals(BigDecimal.valueOf(0.5), attendanceWeight?.modifierValue)
        
        val tankMultiplier = result.find { it.category == "role" && it.modifierKey == "tank_multiplier" }
        assertNotNull(tankMultiplier)
        assertEquals(BigDecimal.valueOf(1.3), tankMultiplier?.modifierValue)
    }

    @Test
    fun `should create entities with correct structure`() {
        // Given
        val guildId = "test-guild"
        val configuration = FlpsConfiguration.default()

        // When
        val result = mapper.toEntities(guildId, configuration)

        // Then
        result.forEach { entity ->
            assertNull(entity.id) // New entities should have null ID
            assertEquals(guildId, entity.guildId)
            assertNotNull(entity.category)
            assertNotNull(entity.modifierKey)
            assertNotNull(entity.modifierValue)
        }
    }

    @Test
    fun `should handle decimal precision correctly`() {
        // Given
        val guildId = "test-guild"
        val entities = listOf(
            createEntity(guildId, "rms", "attendance_weight", 0.123456)
        )

        // When
        val result = mapper.toDomain(entities)

        // Then
        assertEquals(0.123456, result.rmsWeights.attendance, 0.000001)
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
