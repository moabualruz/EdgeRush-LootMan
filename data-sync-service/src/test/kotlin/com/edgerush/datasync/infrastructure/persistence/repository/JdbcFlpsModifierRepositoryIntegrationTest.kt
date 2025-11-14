package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.domain.flps.repository.*
import com.edgerush.datasync.repository.FlpsGuildModifierRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for JdbcFlpsModifierRepository.
 * Tests the repository with a real database using Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcFlpsModifierRepositoryIntegrationTest {

    @Autowired
    private lateinit var repository: FlpsModifierRepository

    @Autowired
    private lateinit var springRepository: FlpsGuildModifierRepository

    @BeforeEach
    fun cleanup() {
        springRepository.deleteAll()
    }

    @Test
    fun `should return default configuration when no guild modifiers exist`() {
        // Given
        val guildId = "integration-test-guild"

        // When
        val result = repository.findByGuildId(guildId)

        // Then
        assertNotNull(result)
        assertEquals(FlpsConfiguration.default(), result)
    }

    @Test
    fun `should save and retrieve guild configuration`() {
        // Given
        val guildId = "integration-test-guild"
        val configuration = FlpsConfiguration(
            rmsWeights = RmsWeights(0.5, 0.3, 0.2),
            ipiWeights = IpiWeights(0.4, 0.4, 0.2),
            thresholds = FlpsThresholds(0.85, 0.05),
            roleMultipliers = RoleMultipliers(1.3, 1.2, 1.0),
            recencyPenalties = RecencyPenalties(0.7, 0.85, 0.95, 0.15)
        )

        // When
        repository.save(guildId, configuration)
        val result = repository.findByGuildId(guildId)

        // Then
        assertNotNull(result)
        assertEquals(configuration.rmsWeights.attendance, result.rmsWeights.attendance)
        assertEquals(configuration.rmsWeights.mechanical, result.rmsWeights.mechanical)
        assertEquals(configuration.rmsWeights.preparation, result.rmsWeights.preparation)
        assertEquals(configuration.ipiWeights.upgradeValue, result.ipiWeights.upgradeValue)
        assertEquals(configuration.ipiWeights.tierBonus, result.ipiWeights.tierBonus)
        assertEquals(configuration.ipiWeights.roleMultiplier, result.ipiWeights.roleMultiplier)
        assertEquals(configuration.thresholds.eligibilityAttendance, result.thresholds.eligibilityAttendance)
        assertEquals(configuration.thresholds.eligibilityActivity, result.thresholds.eligibilityActivity)
        assertEquals(configuration.roleMultipliers.tank, result.roleMultipliers.tank)
        assertEquals(configuration.roleMultipliers.healer, result.roleMultipliers.healer)
        assertEquals(configuration.roleMultipliers.dps, result.roleMultipliers.dps)
        assertEquals(configuration.recencyPenalties.tierA, result.recencyPenalties.tierA)
        assertEquals(configuration.recencyPenalties.tierB, result.recencyPenalties.tierB)
        assertEquals(configuration.recencyPenalties.tierC, result.recencyPenalties.tierC)
        assertEquals(configuration.recencyPenalties.recoveryRate, result.recencyPenalties.recoveryRate)
    }

    @Test
    fun `should update existing configuration`() {
        // Given
        val guildId = "integration-test-guild"
        val initialConfig = FlpsConfiguration(
            rmsWeights = RmsWeights(0.5, 0.3, 0.2),
            ipiWeights = IpiWeights(0.4, 0.4, 0.2),
            thresholds = FlpsThresholds(0.85, 0.05),
            roleMultipliers = RoleMultipliers(1.3, 1.2, 1.0),
            recencyPenalties = RecencyPenalties(0.7, 0.85, 0.95, 0.15)
        )
        repository.save(guildId, initialConfig)

        val updatedConfig = FlpsConfiguration(
            rmsWeights = RmsWeights(0.6, 0.25, 0.15),
            ipiWeights = IpiWeights(0.5, 0.3, 0.2),
            thresholds = FlpsThresholds(0.9, 0.1),
            roleMultipliers = RoleMultipliers(1.4, 1.3, 1.1),
            recencyPenalties = RecencyPenalties(0.6, 0.8, 0.9, 0.2)
        )

        // When
        repository.save(guildId, updatedConfig)
        val result = repository.findByGuildId(guildId)

        // Then
        assertEquals(updatedConfig.rmsWeights.attendance, result.rmsWeights.attendance)
        assertEquals(updatedConfig.ipiWeights.upgradeValue, result.ipiWeights.upgradeValue)
        assertEquals(updatedConfig.thresholds.eligibilityAttendance, result.thresholds.eligibilityAttendance)
        assertEquals(updatedConfig.roleMultipliers.tank, result.roleMultipliers.tank)
        assertEquals(updatedConfig.recencyPenalties.tierA, result.recencyPenalties.tierA)
    }

    @Test
    fun `should handle multiple guilds independently`() {
        // Given
        val guild1 = "guild-1"
        val guild2 = "guild-2"
        val config1 = FlpsConfiguration(
            rmsWeights = RmsWeights(0.5, 0.3, 0.2),
            ipiWeights = IpiWeights(0.4, 0.4, 0.2),
            thresholds = FlpsThresholds(0.85, 0.05),
            roleMultipliers = RoleMultipliers(1.3, 1.2, 1.0),
            recencyPenalties = RecencyPenalties(0.7, 0.85, 0.95, 0.15)
        )
        val config2 = FlpsConfiguration(
            rmsWeights = RmsWeights(0.6, 0.25, 0.15),
            ipiWeights = IpiWeights(0.5, 0.3, 0.2),
            thresholds = FlpsThresholds(0.9, 0.1),
            roleMultipliers = RoleMultipliers(1.4, 1.3, 1.1),
            recencyPenalties = RecencyPenalties(0.6, 0.8, 0.9, 0.2)
        )

        // When
        repository.save(guild1, config1)
        repository.save(guild2, config2)
        val result1 = repository.findByGuildId(guild1)
        val result2 = repository.findByGuildId(guild2)

        // Then
        assertEquals(config1.rmsWeights.attendance, result1.rmsWeights.attendance)
        assertEquals(config2.rmsWeights.attendance, result2.rmsWeights.attendance)
        assertNotEquals(result1.rmsWeights.attendance, result2.rmsWeights.attendance)
    }

    @Test
    fun `should persist all 15 modifier values`() {
        // Given
        val guildId = "integration-test-guild"
        val configuration = FlpsConfiguration.default()

        // When
        repository.save(guildId, configuration)
        val entities = springRepository.findByGuildId(guildId)

        // Then
        assertEquals(15, entities.size) // 3 RMS + 3 IPI + 2 thresholds + 3 roles + 4 recency
    }

    @Test
    fun `should handle decimal precision correctly`() {
        // Given
        val guildId = "integration-test-guild"
        val configuration = FlpsConfiguration(
            rmsWeights = RmsWeights(0.123456, 0.234567, 0.345678),
            ipiWeights = IpiWeights(0.456789, 0.567890, 0.678901),
            thresholds = FlpsThresholds(0.789012, 0.890123),
            roleMultipliers = RoleMultipliers(1.234567, 1.345678, 1.456789),
            recencyPenalties = RecencyPenalties(0.567890, 0.678901, 0.789012, 0.890123)
        )

        // When
        repository.save(guildId, configuration)
        val result = repository.findByGuildId(guildId)

        // Then
        assertEquals(configuration.rmsWeights.attendance, result.rmsWeights.attendance, 0.000001)
        assertEquals(configuration.ipiWeights.upgradeValue, result.ipiWeights.upgradeValue, 0.000001)
        assertEquals(configuration.thresholds.eligibilityAttendance, result.thresholds.eligibilityAttendance, 0.000001)
    }

    @Test
    fun `should delete old modifiers when saving new configuration`() {
        // Given
        val guildId = "integration-test-guild"
        val initialConfig = FlpsConfiguration.default()
        repository.save(guildId, initialConfig)
        val initialCount = springRepository.findByGuildId(guildId).size

        // When
        val newConfig = FlpsConfiguration.default()
        repository.save(guildId, newConfig)
        val finalCount = springRepository.findByGuildId(guildId).size

        // Then
        assertEquals(initialCount, finalCount)
        assertEquals(15, finalCount)
    }
}
