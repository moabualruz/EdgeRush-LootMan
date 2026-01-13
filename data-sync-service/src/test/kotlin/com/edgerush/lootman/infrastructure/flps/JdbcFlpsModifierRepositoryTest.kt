package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Unit tests for JdbcFlpsModifierRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository reads from flps_default_modifiers and flps_guild_modifiers tables.
 */
class JdbcFlpsModifierRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcFlpsModifierRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcFlpsModifierRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByGuildIdTests {

        @Test
        fun `should return default modifiers when guild has no overrides`() {
            // Arrange
            val guildId = GuildId("test-guild")

            // Mock default modifiers query
            mockDefaultModifiers(defaultModifierRows())

            // Mock guild overrides query - empty
            mockGuildOverrides(guildId, emptyList())

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.guildId shouldBe guildId
            modifiers.rmsWeights.attendance shouldBe 0.45
            modifiers.rmsWeights.mechanical shouldBe 0.35
            modifiers.rmsWeights.preparation shouldBe 0.20
        }

        @Test
        fun `should merge guild overrides with defaults`() {
            // Arrange
            val guildId = GuildId("test-guild")

            // Mock default modifiers query
            mockDefaultModifiers(defaultModifierRows())

            // Mock guild overrides - override attendance weight
            mockGuildOverrides(guildId, listOf(
                Triple("rms", "attendance_weight", 0.50)
            ))

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.rmsWeights.attendance shouldBe 0.50 // Overridden
            modifiers.rmsWeights.mechanical shouldBe 0.35 // Default
            modifiers.rmsWeights.preparation shouldBe 0.20 // Default
        }

        @Test
        fun `should return correct IPI weights from database`() {
            // Arrange
            val guildId = GuildId("test-guild")

            mockDefaultModifiers(defaultModifierRows())
            mockGuildOverrides(guildId, emptyList())

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.ipiWeights.upgradeValue shouldBe 0.45
            modifiers.ipiWeights.tierBonus shouldBe 0.35
            modifiers.ipiWeights.roleMultiplier shouldBe 0.20
        }

        @Test
        fun `should return correct role multipliers from database`() {
            // Arrange
            val guildId = GuildId("test-guild")

            mockDefaultModifiers(defaultModifierRows())
            mockGuildOverrides(guildId, emptyList())

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.roleMultipliers.tank shouldBe 1.2
            modifiers.roleMultipliers.healer shouldBe 1.1
            modifiers.roleMultipliers.dps shouldBe 1.0
        }

        @Test
        fun `should return correct thresholds from database`() {
            // Arrange
            val guildId = GuildId("test-guild")

            mockDefaultModifiers(defaultModifierRows())
            mockGuildOverrides(guildId, emptyList())

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.thresholds.eligibilityAttendance shouldBe 0.8
            modifiers.thresholds.eligibilityActivity shouldBe 0.0
        }

        @Test
        fun `should allow guild to override all weights`() {
            // Arrange
            val guildId = GuildId("custom-guild")

            mockDefaultModifiers(defaultModifierRows())

            // Override all RMS weights and threshold
            mockGuildOverrides(guildId, listOf(
                Triple("rms", "attendance_weight", 0.30),
                Triple("rms", "mechanical_weight", 0.50),
                Triple("rms", "preparation_weight", 0.20),
                Triple("threshold", "eligibility_attendance", 0.9)
            ))

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert
            modifiers.rmsWeights.attendance shouldBe 0.30
            modifiers.rmsWeights.mechanical shouldBe 0.50
            modifiers.rmsWeights.preparation shouldBe 0.20
            modifiers.thresholds.eligibilityAttendance shouldBe 0.9
        }

        @Test
        fun `should use hardcoded defaults when database returns empty`() {
            // Arrange
            val guildId = GuildId("empty-guild")

            // Database returns nothing
            mockDefaultModifiers(emptyList())
            mockGuildOverrides(guildId, emptyList())

            // Act
            val modifiers = repository.findByGuildId(guildId)

            // Assert - should use Kotlin default values
            modifiers.guildId shouldBe guildId
            modifiers.rmsWeights.attendance shouldBe 0.4 // Kotlin default
            modifiers.rmsWeights.mechanical shouldBe 0.4 // Kotlin default
            modifiers.rmsWeights.preparation shouldBe 0.2 // Kotlin default
        }
    }

    /**
     * Mock the default modifiers query to return the given rows.
     */
    private fun mockDefaultModifiers(rows: List<Triple<String, String, Double>>) {
        every {
            jdbcTemplate.query(
                match<String> { it.contains("flps_default_modifiers") },
                any<RowMapper<Any>>()
            )
        } answers {
            // The RowMapper is the second argument
            val rowMapper = secondArg<RowMapper<Any>>()
            rows.mapIndexed { index, (category, key, value) ->
                val rs = mockk<ResultSet>()
                every { rs.getString("category") } returns category
                every { rs.getString("modifier_key") } returns key
                every { rs.getDouble("modifier_value") } returns value
                rowMapper.mapRow(rs, index)
            }
        }
    }

    /**
     * Mock the guild overrides query to return the given rows.
     */
    private fun mockGuildOverrides(guildId: GuildId, rows: List<Triple<String, String, Double>>) {
        every {
            jdbcTemplate.query(
                match<String> { it.contains("flps_guild_modifiers") },
                any<RowMapper<Any>>(),
                eq(guildId.value)
            )
        } answers {
            // The RowMapper is the second argument
            val rowMapper = secondArg<RowMapper<Any>>()
            rows.mapIndexed { index, (category, key, value) ->
                val rs = mockk<ResultSet>()
                every { rs.getString("category") } returns category
                every { rs.getString("modifier_key") } returns key
                every { rs.getDouble("modifier_value") } returns value
                rowMapper.mapRow(rs, index)
            }
        }
    }

    /**
     * Returns default modifier rows matching the V0012 migration data.
     */
    private fun defaultModifierRows(): List<Triple<String, String, Double>> = listOf(
        // RMS weights
        Triple("rms", "attendance_weight", 0.45),
        Triple("rms", "mechanical_weight", 0.35),
        Triple("rms", "preparation_weight", 0.20),
        // IPI weights
        Triple("ipi", "upgrade_value_weight", 0.45),
        Triple("ipi", "tier_bonus_weight", 0.35),
        Triple("ipi", "role_multiplier_weight", 0.20),
        // Role multipliers
        Triple("role", "tank_multiplier", 1.2),
        Triple("role", "healer_multiplier", 1.1),
        Triple("role", "dps_multiplier", 1.0),
        // Thresholds
        Triple("threshold", "eligibility_attendance", 0.8),
        Triple("threshold", "eligibility_activity", 0.0),
        Triple("threshold", "recency_decay_days", 30.0),
        // Limits
        Triple("limit", "max_attendance_bonus", 1.0),
        Triple("limit", "min_mechanical_score", 0.0),
        Triple("limit", "max_preparation_score", 1.0),
    )
}
