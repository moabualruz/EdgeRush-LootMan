package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
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
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for JdbcAttendanceRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the attendance_stats table with snake_case columns.
 *
 * Database columns: id (int), character_id, team_id, instance, encounter,
 * start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
 */
class JdbcAttendanceRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcAttendanceRepository

    private val now = Instant.now()
    private val today = LocalDate.now()
    private val oneWeekAgo = today.minusWeeks(1)
    private val oneMonthAgo = today.minusMonths(1)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcAttendanceRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return attendance record when found`() {
            // Given
            val recordId = AttendanceRecordId("123")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(123),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(rowMapper.mapRow(mockResultSet(123), 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "123"
            result?.instance shouldBe "Nerub-ar Palace"
        }

        @Test
        fun `should return null when attendance record not found`() {
            // Given
            val recordId = AttendanceRecordId("999")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(999),
                )
            } returns emptyList()

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val recordId = AttendanceRecordId("456")
            val startDate = LocalDate.of(2024, 6, 1)
            val endDate = LocalDate.of(2024, 6, 30)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(456),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs =
                    mockResultSet(
                        id = 456,
                        characterId = 100L,
                        teamId = 1L,
                        instance = "Vault of the Incarnates",
                        encounter = "Raszageth",
                        startDate = startDate,
                        endDate = endDate,
                        attendedAmount = 8,
                        totalAmount = 10,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "456"
            result?.raiderId?.value shouldBe 100L
            result?.guildId?.value shouldBe "1"
            result?.instance shouldBe "Vault of the Incarnates"
            result?.encounter shouldBe "Raszageth"
            result?.startDate shouldBe startDate
            result?.endDate shouldBe endDate
            result?.attendedRaids shouldBe 8
            result?.totalRaids shouldBe 10
        }

        @Test
        fun `should handle null encounter for overall instance attendance`() {
            // Given
            val recordId = AttendanceRecordId("789")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(789),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs =
                    mockResultSet(
                        id = 789,
                        encounter = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.encounter shouldBe null
        }

        @Test
        fun `should handle null team_id with default value`() {
            // Given - tests branch when team_id is null
            val recordId = AttendanceRecordId("101")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(101),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs =
                    mockResultSetWithNullableFields(
                        id = 101,
                        teamIdNull = true,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.guildId?.value shouldBe "0"
        }

        @Test
        fun `should handle null instance with empty string default`() {
            // Given - tests branch when instance is null
            val recordId = AttendanceRecordId("102")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(102),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs =
                    mockResultSetWithNullableFields(
                        id = 102,
                        instance = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.instance shouldBe ""
        }

        @Test
        fun `should handle null syncedAt with current time default`() {
            // Given - tests branch when synced_at is null
            val recordId = AttendanceRecordId("103")
            val testTimeBeforeCall = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(103),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs =
                    mockResultSetWithNullableFields(
                        id = 103,
                        syncedAt = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)
            val testTimeAfterCall = Instant.now()

            // Then
            result shouldNotBe null
            // The recordedAt should be set to approximately now
            result!!.recordedAt.isAfter(testTimeBeforeCall.minusSeconds(1)) shouldBe true
            result.recordedAt.isBefore(testTimeAfterCall.plusSeconds(1)) shouldBe true
        }
    }

    @Nested
    inner class FindByRaiderIdAndGuildIdAndDateRangeTests {
        @Test
        fun `should return attendance records for raider in date range`() {
            // Given
            val raiderId = RaiderId(100L)
            val guildId = GuildId("1")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("character_id = ?") &&
                            it.contains("team_id = ?")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(1L),
                    any(),
                    any(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1, characterId = raiderId.value, teamId = 1L), 0),
                    rowMapper.mapRow(mockResultSet(2, characterId = raiderId.value, teamId = 1L), 1),
                )
            }

            // When
            val result = repository.findByRaiderIdAndGuildIdAndDateRange(raiderId, guildId, oneMonthAgo, today)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
            result.all { it.guildId == guildId } shouldBe true
        }

        @Test
        fun `should return empty list when no records found`() {
            // Given
            val raiderId = RaiderId(999L)
            val guildId = GuildId("1")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("character_id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(1L),
                    any(),
                    any(),
                )
            } returns emptyList()

            // When
            val result = repository.findByRaiderIdAndGuildIdAndDateRange(raiderId, guildId, oneMonthAgo, today)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByRaiderIdAndGuildIdAndInstanceAndDateRangeTests {
        @Test
        fun `should return records for specific instance`() {
            // Given
            val raiderId = RaiderId(100L)
            val guildId = GuildId("1")
            val instance = "Nerub-ar Palace"

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("character_id = ?") &&
                            it.contains("team_id = ?") &&
                            it.contains("instance = ?")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(1L),
                    eq(instance),
                    any(),
                    any(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1, characterId = raiderId.value, teamId = 1L, instance = instance), 0),
                )
            }

            // When
            val result =
                repository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
                    raiderId,
                    guildId,
                    instance,
                    oneMonthAgo,
                    today,
                )

            // Then
            result.size shouldBe 1
            result[0].instance shouldBe instance
        }
    }

    @Nested
    inner class FindByRaiderIdAndGuildIdAndEncounterAndDateRangeTests {
        @Test
        fun `should return records for specific encounter`() {
            // Given
            val raiderId = RaiderId(100L)
            val guildId = GuildId("1")
            val instance = "Nerub-ar Palace"
            val encounter = "Queen Ansurek"

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("character_id = ?") &&
                            it.contains("team_id = ?") &&
                            it.contains("instance = ?") &&
                            it.contains("encounter = ?")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(1L),
                    eq(instance),
                    eq(encounter),
                    any(),
                    any(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(1, characterId = raiderId.value, teamId = 1L, instance = instance, encounter = encounter),
                        0,
                    ),
                )
            }

            // When
            val result =
                repository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
                    raiderId,
                    guildId,
                    instance,
                    encounter,
                    oneMonthAgo,
                    today,
                )

            // Then
            result.size shouldBe 1
            result[0].encounter shouldBe encounter
        }
    }

    @Nested
    inner class FindByGuildIdAndDateRangeTests {
        @Test
        fun `should return all records for guild in date range`() {
            // Given
            val guildId = GuildId("1")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("team_id = ?") &&
                            !it.contains("character_id = ?")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(1L),
                    any(),
                    any(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1, characterId = 100L, teamId = 1L), 0),
                    rowMapper.mapRow(mockResultSet(2, characterId = 101L, teamId = 1L), 1),
                    rowMapper.mapRow(mockResultSet(3, characterId = 102L, teamId = 1L), 2),
                )
            }

            // When
            val result = repository.findByGuildIdAndDateRange(guildId, oneMonthAgo, today)

            // Then
            result.size shouldBe 3
            result.all { it.guildId == guildId } shouldBe true
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new attendance record when not exists`() {
            // Given
            val record = createAttendanceRecord(id = AttendanceRecordId("200"))
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, eq(200)) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(record)

            // Then
            result shouldBe record
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should update existing attendance record when exists`() {
            // Given
            val record = createAttendanceRecord(id = AttendanceRecordId("201"))
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, eq(201)) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(record)

            // Then
            result shouldBe record
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete attendance record by id`() {
            // Given
            val recordId = AttendanceRecordId("300")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(300),
                )
            } returns 1

            // When
            repository.delete(recordId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    300,
                )
            }
        }
    }

    // Helper methods

    /**
     * Creates a mock ResultSet matching the actual repository's RowMapper column access.
     * Database columns: id, character_id, team_id, instance, encounter,
     * start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
     */
    private fun mockResultSet(
        id: Int,
        characterId: Long = 100L,
        teamId: Long = 1L,
        instance: String = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedAmount: Int = 8,
        totalAmount: Int = 10,
        syncedAt: Instant = now,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getInt("id") } returns id
        every { rs.getLong("character_id") } returns characterId
        every { rs.getLong("team_id") } returns teamId
        every { rs.wasNull() } returns false
        every { rs.getString("instance") } returns instance
        every { rs.getString("encounter") } returns encounter
        every { rs.getDate("start_date") } returns Date.valueOf(startDate)
        every { rs.getDate("end_date") } returns Date.valueOf(endDate)
        every { rs.getInt("attended_amount_of_raids") } returns attendedAmount
        every { rs.getInt("total_amount_of_raids") } returns totalAmount
        every { rs.getTimestamp("synced_at") } returns Timestamp.from(syncedAt)
        return rs
    }

    /**
     * Creates a mock ResultSet with nullable fields for testing edge cases.
     */
    private fun mockResultSetWithNullableFields(
        id: Int,
        characterId: Long = 100L,
        teamIdNull: Boolean = false,
        instance: String? = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedAmount: Int = 8,
        totalAmount: Int = 10,
        syncedAt: Timestamp? = Timestamp.from(now),
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getInt("id") } returns id
        every { rs.getLong("character_id") } returns characterId
        every { rs.getLong("team_id") } returns if (teamIdNull) 0L else 1L
        every { rs.wasNull() } returns teamIdNull
        every { rs.getString("instance") } returns instance
        every { rs.getString("encounter") } returns encounter
        every { rs.getDate("start_date") } returns Date.valueOf(startDate)
        every { rs.getDate("end_date") } returns Date.valueOf(endDate)
        every { rs.getInt("attended_amount_of_raids") } returns attendedAmount
        every { rs.getInt("total_amount_of_raids") } returns totalAmount
        every { rs.getTimestamp("synced_at") } returns syncedAt
        return rs
    }

    /**
     * Creates an AttendanceRecord with a specific ID for testing.
     * Uses reflection to access the private constructor since the domain model
     * is designed to be created through the companion object's create() method.
     */
    private fun createAttendanceRecord(
        id: AttendanceRecordId = AttendanceRecordId("100"),
        raiderId: RaiderId = RaiderId(100L),
        guildId: GuildId = GuildId("1"),
        instance: String = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedRaids: Int = 8,
        totalRaids: Int = 10,
    ): AttendanceRecord {
        val constructor =
            AttendanceRecord::class.java.getDeclaredConstructor(
                AttendanceRecordId::class.java,
                RaiderId::class.java,
                GuildId::class.java,
                String::class.java,
                String::class.java,
                LocalDate::class.java,
                LocalDate::class.java,
                Int::class.java,
                Int::class.java,
                Instant::class.java,
            )
        constructor.isAccessible = true
        return constructor.newInstance(
            id,
            raiderId,
            guildId,
            instance,
            encounter,
            startDate,
            endDate,
            attendedRaids,
            totalRaids,
            Instant.now(),
        )
    }
}
