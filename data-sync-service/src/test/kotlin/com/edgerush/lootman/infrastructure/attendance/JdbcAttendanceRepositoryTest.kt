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
 * The repository operates on the attendance_stats table.
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
            val recordId = AttendanceRecordId("test-record-id")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(rowMapper.mapRow(mockResultSet(recordId.value), 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.id shouldBe recordId
            result?.instance shouldBe "Nerub-ar Palace"
        }

        @Test
        fun `should return null when attendance record not found`() {
            // Given
            val recordId = AttendanceRecordId("non-existent")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
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
            val recordId = AttendanceRecordId("full-record")
            val startDate = LocalDate.of(2024, 6, 1)
            val endDate = LocalDate.of(2024, 6, 30)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs = mockResultSet(
                    id = recordId.value,
                    raiderId = 100L,
                    guildId = "test-guild",
                    instance = "Vault of the Incarnates",
                    encounter = "Raszageth",
                    startDate = startDate,
                    endDate = endDate,
                    attendedRaids = 8,
                    totalRaids = 10
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "full-record"
            result?.raiderId?.value shouldBe 100L
            result?.guildId?.value shouldBe "test-guild"
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
            val recordId = AttendanceRecordId("instance-only")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs = mockResultSet(
                    id = recordId.value,
                    encounter = null
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
            // Given - tests branch: rs.getString("team_id") ?: "default"
            val recordId = AttendanceRecordId("null-team-record")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs = mockResultSetWithNullableFields(
                    id = recordId.value,
                    teamId = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(recordId)

            // Then
            result shouldNotBe null
            result?.guildId?.value shouldBe "default"
        }

        @Test
        fun `should handle null instance with empty string default`() {
            // Given - tests branch: rs.getString("instance") ?: ""
            val recordId = AttendanceRecordId("null-instance-record")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs = mockResultSetWithNullableFields(
                    id = recordId.value,
                    instance = null
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
            // Given - tests branch: rs.getTimestamp("syncedAt")?.toInstant() ?: Instant.now()
            val recordId = AttendanceRecordId("null-synced-record")
            val testTimeBeforeCall = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(recordId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                val rs = mockResultSetWithNullableFields(
                    id = recordId.value,
                    syncedAt = null
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
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("character_id = ?") &&
                            it.contains("team_id = ?") &&
                            it.contains("startDate") &&
                            it.contains("endDate")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any(),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("record-1", raiderId = raiderId.value, guildId = guildId.value), 0),
                    rowMapper.mapRow(mockResultSet("record-2", raiderId = raiderId.value, guildId = guildId.value), 1)
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
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("character_id = ?") },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any(),
                    any()
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
            val guildId = GuildId("test-guild")
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
                    eq(guildId.value),
                    eq(instance),
                    any(),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("record-1", raiderId = raiderId.value, guildId = guildId.value, instance = instance), 0)
                )
            }

            // When
            val result = repository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
                raiderId, guildId, instance, oneMonthAgo, today
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
            val guildId = GuildId("test-guild")
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
                    eq(guildId.value),
                    eq(instance),
                    eq(encounter),
                    any(),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet("record-1", raiderId = raiderId.value, guildId = guildId.value, instance = instance, encounter = encounter),
                        0
                    )
                )
            }

            // When
            val result = repository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
                raiderId, guildId, instance, encounter, oneMonthAgo, today
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
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("team_id = ?") &&
                            !it.contains("character_id = ?")
                    },
                    any<RowMapper<AttendanceRecord>>(),
                    eq(guildId.value),
                    any(),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AttendanceRecord>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("record-1", raiderId = 100L, guildId = guildId.value), 0),
                    rowMapper.mapRow(mockResultSet("record-2", raiderId = 101L, guildId = guildId.value), 1),
                    rowMapper.mapRow(mockResultSet("record-3", raiderId = 102L, guildId = guildId.value), 2)
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
            val record = createAttendanceRecord()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, record.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(record)

            // Then
            result shouldBe record
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should update existing attendance record when exists`() {
            // Given
            val record = createAttendanceRecord()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, record.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(record)

            // Then
            result shouldBe record
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg()
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete attendance record by id`() {
            // Given
            val recordId = AttendanceRecordId("to-delete")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(recordId.value)
                )
            } returns 1

            // When
            repository.delete(recordId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    recordId.value
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: String,
        raiderId: Long = 100L,
        guildId: String = "test-guild",
        instance: String = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedRaids: Int = 8,
        totalRaids: Int = 10,
        recordedAt: Instant = now
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("id") } returns id
        every { rs.getLong("character_id") } returns raiderId
        every { rs.getString("team_id") } returns guildId
        every { rs.getString("instance") } returns instance
        every { rs.getString("encounter") } returns encounter
        every { rs.getDate("startDate") } returns Date.valueOf(startDate)
        every { rs.getDate("endDate") } returns Date.valueOf(endDate)
        every { rs.getInt("attendedAmount") } returns attendedRaids
        every { rs.getInt("totalAmount") } returns totalRaids
        every { rs.getTimestamp("syncedAt") } returns Timestamp.from(recordedAt)
        return rs
    }

    private fun mockResultSetWithNullableFields(
        id: String,
        raiderId: Long = 100L,
        teamId: String? = "test-guild",
        instance: String? = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedRaids: Int = 8,
        totalRaids: Int = 10,
        syncedAt: Timestamp? = Timestamp.from(now)
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("id") } returns id
        every { rs.getLong("character_id") } returns raiderId
        every { rs.getString("team_id") } returns teamId
        every { rs.getString("instance") } returns instance
        every { rs.getString("encounter") } returns encounter
        every { rs.getDate("startDate") } returns Date.valueOf(startDate)
        every { rs.getDate("endDate") } returns Date.valueOf(endDate)
        every { rs.getInt("attendedAmount") } returns attendedRaids
        every { rs.getInt("totalAmount") } returns totalRaids
        every { rs.getTimestamp("syncedAt") } returns syncedAt
        return rs
    }

    private fun createAttendanceRecord(
        id: AttendanceRecordId = AttendanceRecordId("test-record"),
        raiderId: RaiderId = RaiderId(100L),
        guildId: GuildId = GuildId("test-guild"),
        instance: String = "Nerub-ar Palace",
        encounter: String? = "Ulgrax",
        startDate: LocalDate = oneMonthAgo,
        endDate: LocalDate = today,
        attendedRaids: Int = 8,
        totalRaids: Int = 10
    ): AttendanceRecord = createAttendanceRecordWithId(
        id = id,
        raiderId = raiderId,
        guildId = guildId,
        instance = instance,
        encounter = encounter,
        startDate = startDate,
        endDate = endDate,
        attendedRaids = attendedRaids,
        totalRaids = totalRaids
    )

    /**
     * Helper to create AttendanceRecord with a specific ID for testing.
     * Since AttendanceRecord.create() generates a new ID, we use reflection
     * or a test-specific construction approach.
     */
    private fun createAttendanceRecordWithId(
        id: AttendanceRecordId,
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        attendedRaids: Int,
        totalRaids: Int
    ): AttendanceRecord {
        // Use the private constructor via reflection for testing
        val constructor = AttendanceRecord::class.java.getDeclaredConstructor(
            AttendanceRecordId::class.java,
            RaiderId::class.java,
            GuildId::class.java,
            String::class.java,
            String::class.java,
            LocalDate::class.java,
            LocalDate::class.java,
            Int::class.java,
            Int::class.java,
            Instant::class.java
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
            Instant.now()
        )
    }
}
