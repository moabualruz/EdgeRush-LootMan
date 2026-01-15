package com.edgerush.lootman.infrastructure.guest

import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.datasync.test.base.UnitTest
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
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Unit tests for JdbcGuestRepository.
 */
class JdbcGuestRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcGuestRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcGuestRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return guest when found`() {
            val guestId = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("guest_id = ?")
                    },
                    any<RowMapper<GuestEntity>>(), eq(guestId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuestEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(guestId), 0))
            }
            val result = repository.findById(guestId)
            result shouldNotBe null
            result?.guestId shouldBe guestId
        }

        @Test
        fun `should return null when guest not found`() {
            val guestId = 999L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("guest_id = ?")
                    },
                    any<RowMapper<GuestEntity>>(), eq(guestId),
                )
            } returns emptyList()
            repository.findById(guestId) shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            val guestId = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("guest_id = ?")
                    },
                    any<RowMapper<GuestEntity>>(), eq(guestId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuestEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(guestId, name = "TestGuest", realm = "Illidan", clazz = "Mage", role = "DPS", blizzardId = 12345L), 0))
            }
            val result = repository.findById(guestId)
            result shouldNotBe null
            result?.name shouldBe "TestGuest"
            result?.realm shouldBe "Illidan"
            result?.clazz shouldBe "Mage"
            result?.role shouldBe "DPS"
            result?.blizzardId shouldBe 12345L
        }

        @Test
        fun `should handle null optional fields`() {
            val guestId = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("guest_id = ?")
                    },
                    any<RowMapper<GuestEntity>>(), eq(guestId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuestEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(guestId, realm = null, clazz = null, role = null, blizzardId = null, trackingSince = null), 0))
            }
            val result = repository.findById(guestId)
            result shouldNotBe null
            result?.realm shouldBe null
            result?.clazz shouldBe null
            result?.role shouldBe null
            result?.blizzardId shouldBe null
            result?.trackingSince shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated guests`() {
            val offset = 10L
            val limit = 5
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET")
                    },
                    any<RowMapper<GuestEntity>>(), eq(limit), eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuestEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L), 0), rowMapper.mapRow(mockResultSet(2L), 1))
            }
            val result = repository.findAll(offset, limit)
            result.size shouldBe 2
        }

        @Test
        fun `should return empty list when no guests`() {
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("LIMIT")
                    },
                    any<RowMapper<GuestEntity>>(), any<Int>(), any<Long>(),
                )
            } returns emptyList()
            repository.findAll(0L, 10) shouldBe emptyList()
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            every { jdbcTemplate.queryForObject(match<String> { it.contains("COUNT(*)") && it.contains("guests") }, Long::class.java) } returns 42L
            repository.count() shouldBe 42L
        }

        @Test
        fun `should handle null count result`() {
            every { jdbcTemplate.queryForObject(match<String> { it.contains("COUNT(*)") }, Long::class.java) } returns null
            repository.count() shouldBe 0L
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when guest exists`() {
            val guestId = 1L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("guest_id = ?")
                    },
                    Int::class.java, eq(guestId),
                )
            } returns 1
            repository.existsById(guestId) shouldBe true
        }

        @Test
        fun `should return false when guest does not exist`() {
            val guestId = 999L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("guest_id = ?")
                    },
                    Int::class.java, eq(guestId),
                )
            } returns 0
            repository.existsById(guestId) shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            val guestId = 1L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("guest_id = ?")
                    },
                    Int::class.java, eq(guestId),
                )
            } returns null
            repository.existsById(guestId) shouldBe false
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new guest when not exists`() {
            val entity = createEntity()
            val sqlSlot = slot<String>()
            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.guestId) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1
            val result = repository.save(entity)
            result shouldBe entity
            sqlSlot.captured.contains("INSERT") shouldBe true
        }

        @Test
        fun `should update existing guest when exists`() {
            val entity = createEntity()
            val sqlSlot = slot<String>()
            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.guestId) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1
            val result = repository.save(entity)
            result shouldBe entity
            sqlSlot.captured.contains("UPDATE") shouldBe true
        }

        @Test
        fun `should handle null tracking since`() {
            val entity = createEntity(trackingSince = null)
            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.guestId) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1
            val result = repository.save(entity)
            result.trackingSince shouldBe null
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete guest by id`() {
            val guestId = 1L
            every { jdbcTemplate.update(match<String> { it.contains("DELETE") }, eq(guestId)) } returns 1
            repository.delete(guestId)
            verify { jdbcTemplate.update(match { it.contains("DELETE") && it.contains("guest_id = ?") }, guestId) }
        }
    }

    private fun mockResultSet(
        guestId: Long,
        name: String = "TestGuest",
        realm: String? = "Illidan",
        clazz: String? = "Mage",
        role: String? = "DPS",
        blizzardId: Long? = 12345L,
        trackingSince: OffsetDateTime? = now,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("guest_id") } returns guestId
        every { rs.getString("name") } returns name
        every { rs.getString("realm") } returns realm
        every { rs.getString("class") } returns clazz
        every { rs.getString("role") } returns role
        every { rs.getLong("blizzard_id") } returns (blizzardId ?: 0L)
        every { rs.wasNull() } returns (blizzardId == null)
        every { rs.getTimestamp("tracking_since") } returns trackingSince?.let { Timestamp.from(it.toInstant()) }
        every { rs.getTimestamp("synced_at") } returns Timestamp.from(now.toInstant())
        return rs
    }

    private fun createEntity(
        guestId: Long = 1L,
        name: String = "TestGuest",
        realm: String? = "Illidan",
        clazz: String? = "Mage",
        role: String? = "DPS",
        blizzardId: Long? = 12345L,
        trackingSince: OffsetDateTime? = now,
        syncedAt: OffsetDateTime = now,
    ) = GuestEntity(guestId, name, realm, clazz, role, blizzardId, trackingSince, syncedAt)
}
