package com.edgerush.lootman.infrastructure.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

/**
 * Unit tests for JdbcGuildRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the guild_configurations table.
 */
class JdbcGuildRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcGuildRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcGuildRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return guild when found by id`() {
            // Given
            val guildId = GuildId("test-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                listOf(rowMapper.mapRow(mockResultSet(guildId.value, "Test Guild", now), 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result shouldNotBe null
            result?.id shouldBe guildId
            result?.name shouldBe "Test Guild"
            result?.region shouldBe Region.US
            result?.isActive shouldBe true
        }

        @Test
        fun `should return null when guild not found`() {
            // Given
            val guildId = GuildId("non-existent")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findById(guildId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val guildId = GuildId("full-guild")
            val createdAt = Instant.parse("2024-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2024-06-15T12:00:00Z")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                val rs =
                    mockResultSet(
                        guildId = guildId.value,
                        name = "Full Guild",
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        description = "A complete guild configuration",
                        realm = "Area 52",
                        region = "EU",
                        syncEnabled = true,
                        syncCronExpression = "0 0 5 * * *",
                        syncRunOnStartup = true,
                        timezone = "America/New_York",
                        benchmarkMode = "CUSTOM",
                        customBenchmarkRms = 0.95,
                        customBenchmarkIpi = 0.90,
                        syncStatus = "SUCCESS",
                        isActive = true,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "full-guild"
            result?.name shouldBe "Full Guild"
            result?.description shouldBe "A complete guild configuration"
            result?.realm shouldBe "Area 52"
            result?.region shouldBe Region.EU
            result?.settings?.syncEnabled shouldBe true
            result?.settings?.syncCronExpression shouldBe "0 0 5 * * *"
            result?.settings?.syncRunOnStartup shouldBe true
            result?.settings?.timezone shouldBe "America/New_York"
            result?.settings?.benchmarkMode shouldBe BenchmarkMode.CUSTOM
            result?.settings?.customBenchmarkRms shouldBe 0.95
            result?.settings?.customBenchmarkIpi shouldBe 0.90
            result?.syncStatus shouldBe SyncStatus.SUCCESS
            result?.isActive shouldBe true
            result?.createdAt shouldBe createdAt
            result?.updatedAt shouldBe updatedAt
        }

        @Test
        fun `should handle null sync_cron_expression with default value`() {
            // Given - tests branch: rs.getString("sync_cron_expression") ?: "0 0 4 * * *"
            val guildId = GuildId("default-cron-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                val rs = mockResultSetWithNullCronAndTimezone(guildId.value, "Default Cron Guild", now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result?.settings?.syncCronExpression shouldBe "0 0 4 * * *"
            result?.settings?.timezone shouldBe "UTC"
        }

        @Test
        fun `should handle unknown syncStatus with default NEVER_RUN`() {
            // Given - tests branch: SyncStatus.fromString returns null
            val guildId = GuildId("unknown-status-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                val rs = mockResultSetWithUnknownEnums(guildId.value, "Unknown Status Guild", now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result?.syncStatus shouldBe SyncStatus.NEVER_RUN
            result?.region shouldBe Region.US
            result?.settings?.benchmarkMode shouldBe BenchmarkMode.THEORETICAL
        }

        @Test
        fun `should handle null region string with default US`() {
            // Given - tests branch: rs.getString("region") ?: "US"
            val guildId = GuildId("null-region-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                val rs = mockResultSetWithNullRegion(guildId.value, "Null Region Guild", now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result?.region shouldBe Region.US
        }

        @Test
        fun `should handle null benchmark_mode string with default THEORETICAL`() {
            // Given - tests branch: rs.getString("benchmark_mode") ?: "THEORETICAL"
            val guildId = GuildId("null-benchmark-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Guild>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                val rs = mockResultSetWithNullBenchmarkMode(guildId.value, "Null Benchmark Guild", now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(guildId)

            // Then
            result?.settings?.benchmarkMode shouldBe BenchmarkMode.THEORETICAL
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return all guilds`() {
            // Given
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && !it.contains("is_active = true") },
                    any<RowMapper<Guild>>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("guild-1", "Guild One", now), 0),
                    rowMapper.mapRow(mockResultSet("guild-2", "Guild Two", now, isActive = false), 1),
                    rowMapper.mapRow(mockResultSet("guild-3", "Guild Three", now), 2),
                )
            }

            // When
            val result = repository.findAll()

            // Then
            result.size shouldBe 3
            result[0].id.value shouldBe "guild-1"
            result[1].id.value shouldBe "guild-2"
            result[1].isActive shouldBe false
            result[2].id.value shouldBe "guild-3"
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<Guild>>(),
                )
            } returns emptyList()

            // When
            val result = repository.findAll()

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindAllActiveTests {
        @Test
        fun `should return only active guilds`() {
            // Given
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("is_active = true") },
                    any<RowMapper<Guild>>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Guild>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("active-1", "Active One", now), 0),
                    rowMapper.mapRow(mockResultSet("active-2", "Active Two", now), 1),
                )
            }

            // When
            val result = repository.findAllActive()

            // Then
            result.size shouldBe 2
            result.all { it.isActive } shouldBe true
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new guild when not exists`() {
            // Given
            val guild = createGuild(id = GuildId("new-guild"))
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, guild.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(guild)

            // Then
            result shouldBe guild
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    guild.id.value,
                    guild.name,
                    guild.description,
                    guild.realm,
                    guild.region.name,
                    guild.settings.syncEnabled,
                    guild.settings.syncCronExpression,
                    guild.settings.syncRunOnStartup,
                    guild.syncStatus.name,
                    guild.settings.timezone,
                    guild.settings.benchmarkMode.name,
                    guild.settings.customBenchmarkRms,
                    guild.settings.customBenchmarkIpi,
                    guild.isActive,
                )
            }
        }

        @Test
        fun `should update existing guild when exists`() {
            // Given
            val guild = createGuild(id = GuildId("existing-guild"), name = "Updated Name")
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, guild.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(guild)

            // Then
            result shouldBe guild
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    guild.name,
                    guild.description,
                    guild.realm,
                    guild.region.name,
                    guild.settings.syncEnabled,
                    guild.settings.syncCronExpression,
                    guild.settings.syncRunOnStartup,
                    guild.syncStatus.name,
                    guild.settings.timezone,
                    guild.settings.benchmarkMode.name,
                    guild.settings.customBenchmarkRms,
                    guild.settings.customBenchmarkIpi,
                    guild.isActive,
                    guild.id.value,
                )
            }
        }
    }

    @Nested
    inner class DeleteByIdTests {
        @Test
        fun `should return true when guild deleted`() {
            // Given
            val guildId = GuildId("to-delete")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(guildId.value),
                )
            } returns 1

            // When
            val result = repository.deleteById(guildId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when guild not found`() {
            // Given
            val guildId = GuildId("non-existent")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(guildId.value),
                )
            } returns 0

            // When
            val result = repository.deleteById(guildId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when guild exists`() {
            // Given
            val guildId = GuildId("existing")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Int::class.java,
                    eq(guildId.value),
                )
            } returns 1

            // When
            val result = repository.existsById(guildId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when guild does not exist`() {
            // Given
            val guildId = GuildId("non-existent")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Int::class.java,
                    eq(guildId.value),
                )
            } returns 0

            // When
            val result = repository.existsById(guildId)

            // Then
            result shouldBe false
        }
    }

    // Helper methods

    private fun mockResultSet(
        guildId: String,
        name: String,
        createdAt: Instant,
        updatedAt: Instant = createdAt,
        description: String? = null,
        realm: String? = null,
        region: String = "US",
        syncEnabled: Boolean = true,
        syncCronExpression: String = "0 0 4 * * *",
        syncRunOnStartup: Boolean = false,
        timezone: String = "UTC",
        benchmarkMode: String = "THEORETICAL",
        customBenchmarkRms: Double? = null,
        customBenchmarkIpi: Double? = null,
        syncStatus: String? = null,
        isActive: Boolean = true,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("guild_name") } returns name
        every { rs.getString("guild_description") } returns description
        every { rs.getString("realm") } returns realm
        every { rs.getString("region") } returns region
        every { rs.getBoolean("sync_enabled") } returns syncEnabled
        every { rs.getString("sync_cron_expression") } returns syncCronExpression
        every { rs.getBoolean("sync_run_on_startup") } returns syncRunOnStartup
        every { rs.getString("timezone") } returns timezone
        every { rs.getString("benchmark_mode") } returns benchmarkMode
        every { rs.getDouble("custom_benchmark_rms") } returns (customBenchmarkRms ?: 0.0)
        every { rs.wasNull() } returns (customBenchmarkRms == null)
        every { rs.getDouble("custom_benchmark_ipi") } returns (customBenchmarkIpi ?: 0.0)
        every { rs.getString("last_sync_status") } returns syncStatus
        every { rs.getBoolean("is_active") } returns isActive
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(updatedAt)
        return rs
    }

    private fun mockResultSetWithNullCronAndTimezone(
        guildId: String,
        name: String,
        createdAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("guild_name") } returns name
        every { rs.getString("guild_description") } returns null
        every { rs.getString("realm") } returns null
        every { rs.getString("region") } returns "US"
        every { rs.getBoolean("sync_enabled") } returns true
        every { rs.getString("sync_cron_expression") } returns null // null cron
        every { rs.getBoolean("sync_run_on_startup") } returns false
        every { rs.getString("timezone") } returns null // null timezone
        every { rs.getString("benchmark_mode") } returns "THEORETICAL"
        every { rs.getDouble("custom_benchmark_rms") } returns 0.0
        every { rs.wasNull() } returns true
        every { rs.getDouble("custom_benchmark_ipi") } returns 0.0
        every { rs.getString("last_sync_status") } returns null
        every { rs.getBoolean("is_active") } returns true
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(createdAt)
        return rs
    }

    private fun mockResultSetWithUnknownEnums(
        guildId: String,
        name: String,
        createdAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("guild_name") } returns name
        every { rs.getString("guild_description") } returns null
        every { rs.getString("realm") } returns null
        every { rs.getString("region") } returns "UNKNOWN_REGION" // invalid region
        every { rs.getBoolean("sync_enabled") } returns true
        every { rs.getString("sync_cron_expression") } returns "0 0 4 * * *"
        every { rs.getBoolean("sync_run_on_startup") } returns false
        every { rs.getString("timezone") } returns "UTC"
        every { rs.getString("benchmark_mode") } returns "UNKNOWN_MODE" // invalid benchmark mode
        every { rs.getDouble("custom_benchmark_rms") } returns 0.0
        every { rs.wasNull() } returns true
        every { rs.getDouble("custom_benchmark_ipi") } returns 0.0
        every { rs.getString("last_sync_status") } returns "UNKNOWN_STATUS" // invalid sync status
        every { rs.getBoolean("is_active") } returns true
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(createdAt)
        return rs
    }

    private fun mockResultSetWithNullRegion(
        guildId: String,
        name: String,
        createdAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("guild_name") } returns name
        every { rs.getString("guild_description") } returns null
        every { rs.getString("realm") } returns null
        every { rs.getString("region") } returns null // null region
        every { rs.getBoolean("sync_enabled") } returns true
        every { rs.getString("sync_cron_expression") } returns "0 0 4 * * *"
        every { rs.getBoolean("sync_run_on_startup") } returns false
        every { rs.getString("timezone") } returns "UTC"
        every { rs.getString("benchmark_mode") } returns "THEORETICAL"
        every { rs.getDouble("custom_benchmark_rms") } returns 0.0
        every { rs.wasNull() } returns true
        every { rs.getDouble("custom_benchmark_ipi") } returns 0.0
        every { rs.getString("last_sync_status") } returns null
        every { rs.getBoolean("is_active") } returns true
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(createdAt)
        return rs
    }

    private fun mockResultSetWithNullBenchmarkMode(
        guildId: String,
        name: String,
        createdAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("guild_name") } returns name
        every { rs.getString("guild_description") } returns null
        every { rs.getString("realm") } returns null
        every { rs.getString("region") } returns "US"
        every { rs.getBoolean("sync_enabled") } returns true
        every { rs.getString("sync_cron_expression") } returns "0 0 4 * * *"
        every { rs.getBoolean("sync_run_on_startup") } returns false
        every { rs.getString("timezone") } returns "UTC"
        every { rs.getString("benchmark_mode") } returns null // null benchmark mode
        every { rs.getDouble("custom_benchmark_rms") } returns 0.0
        every { rs.wasNull() } returns true
        every { rs.getDouble("custom_benchmark_ipi") } returns 0.0
        every { rs.getString("last_sync_status") } returns null
        every { rs.getBoolean("is_active") } returns true
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(createdAt)
        return rs
    }

    private fun createGuild(
        id: GuildId = GuildId("test-guild"),
        name: String = "Test Guild",
        description: String? = null,
        realm: String? = null,
        region: Region = Region.US,
        settings: GuildSettings = GuildSettings.default(),
        syncStatus: SyncStatus = SyncStatus.NEVER_RUN,
        isActive: Boolean = true,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Guild =
        Guild(
            id = id,
            name = name,
            description = description,
            realm = realm,
            region = region,
            settings = settings,
            syncStatus = syncStatus,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
