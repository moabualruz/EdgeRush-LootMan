package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.flps.repository.FlpsConfiguration
import com.edgerush.datasync.domain.flps.repository.FlpsModifierRepository
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.datasync.infrastructure.persistence.mapper.FlpsModifierMapper
import com.edgerush.datasync.repository.FlpsGuildModifierRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Unit tests for JdbcFlpsModifierRepository.
 * Tests the repository implementation that bridges domain and infrastructure layers.
 */
class JdbcFlpsModifierRepositoryTest {

    private lateinit var springRepository: FlpsGuildModifierRepository
    private lateinit var mapper: FlpsModifierMapper
    private lateinit var repository: FlpsModifierRepository

    @BeforeEach
    fun setup() {
        springRepository = mockk()
        mapper = FlpsModifierMapper()
        repository = JdbcFlpsModifierRepository(springRepository, mapper)
    }

    @Test
    fun `should return default configuration when no guild modifiers exist`() {
        // Given
        val guildId = "test-guild"
        every { springRepository.findByGuildId(guildId) } returns emptyList()

        // When
        val result = repository.findByGuildId(guildId)

        // Then
        assertNotNull(result)
        assertEquals(FlpsConfiguration.default(), result)
        verify(exactly = 1) { springRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should map guild modifiers to configuration when modifiers exist`() {
        // Given
        val guildId = "test-guild"
        val entities = listOf(
            createModifierEntity(guildId, "rms", "attendance_weight", 0.5),
            createModifierEntity(guildId, "rms", "mechanical_weight", 0.3),
            createModifierEntity(guildId, "rms", "preparation_weight", 0.2),
            createModifierEntity(guildId, "ipi", "upgrade_value_weight", 0.4),
            createModifierEntity(guildId, "ipi", "tier_bonus_weight", 0.4),
            createModifierEntity(guildId, "ipi", "role_multiplier_weight", 0.2),
            createModifierEntity(guildId, "role", "tank_multiplier", 1.2),
            createModifierEntity(guildId, "role", "healer_multiplier", 1.1),
            createModifierEntity(guildId, "role", "dps_multiplier", 1.0),
            createModifierEntity(guildId, "threshold", "eligibility_attendance", 0.75),
            createModifierEntity(guildId, "threshold", "eligibility_activity", 0.1)
        )
        every { springRepository.findByGuildId(guildId) } returns entities

        // When
        val result = repository.findByGuildId(guildId)

        // Then
        assertNotNull(result)
        assertEquals(0.5, result.rmsWeights.attendance)
        assertEquals(0.3, result.rmsWeights.mechanical)
        assertEquals(0.2, result.rmsWeights.preparation)
        assertEquals(0.4, result.ipiWeights.upgradeValue)
        assertEquals(0.4, result.ipiWeights.tierBonus)
        assertEquals(0.2, result.ipiWeights.roleMultiplier)
        assertEquals(1.2, result.roleMultipliers.tank)
        assertEquals(1.1, result.roleMultipliers.healer)
        assertEquals(1.0, result.roleMultipliers.dps)
        assertEquals(0.75, result.thresholds.eligibilityAttendance)
        assertEquals(0.1, result.thresholds.eligibilityActivity)
    }

    @Test
    fun `should use default values for missing modifiers`() {
        // Given
        val guildId = "test-guild"
        val entities = listOf(
            createModifierEntity(guildId, "rms", "attendance_weight", 0.6)
            // Only one modifier, rest should use defaults
        )
        every { springRepository.findByGuildId(guildId) } returns entities

        // When
        val result = repository.findByGuildId(guildId)

        // Then
        assertNotNull(result)
        assertEquals(0.6, result.rmsWeights.attendance) // Custom value
        // These should be defaults
        assertEquals(FlpsConfiguration.default().rmsWeights.mechanical, result.rmsWeights.mechanical)
        assertEquals(FlpsConfiguration.default().rmsWeights.preparation, result.rmsWeights.preparation)
    }

    @Test
    fun `should save configuration as guild modifiers`() {
        // Given
        val guildId = "test-guild"
        val configuration = FlpsConfiguration.default()
        every { springRepository.deleteByGuildId(guildId) } returns Unit
        every { springRepository.save(any()) } answers { firstArg() }

        // When
        repository.save(guildId, configuration)

        // Then
        verify(exactly = 1) { springRepository.deleteByGuildId(guildId) }
        verify(atLeast = 1) { springRepository.save(any()) }
    }

    @Test
    fun `should delete existing modifiers before saving new ones`() {
        // Given
        val guildId = "test-guild"
        val configuration = FlpsConfiguration.default()
        every { springRepository.deleteByGuildId(guildId) } returns Unit
        every { springRepository.save(any()) } answers { firstArg() }

        // When
        repository.save(guildId, configuration)

        // Then
        verify(exactly = 1) { springRepository.deleteByGuildId(guildId) }
    }

    private fun createModifierEntity(
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
            description = null,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
    }
}
