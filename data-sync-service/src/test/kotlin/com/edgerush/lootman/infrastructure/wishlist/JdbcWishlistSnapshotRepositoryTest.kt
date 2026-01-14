package com.edgerush.lootman.infrastructure.wishlist

import com.edgerush.datasync.entity.WishlistSnapshotEntity
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
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Unit tests for JdbcWishlistSnapshotRepository.
 */
class JdbcWishlistSnapshotRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcWishlistSnapshotRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcWishlistSnapshotRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return snapshot when found`() {
            val id = 1L
            every { jdbcTemplate.query(match<String> { it.contains("SELECT") && it.contains("id = ?") }, any<RowMapper<WishlistSnapshotEntity>>(), eq(id)) } answers {
                val rowMapper = secondArg<RowMapper<WishlistSnapshotEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.id shouldBe id
        }

        @Test
        fun `should return null when snapshot not found`() {
            val id = 999L
            every { jdbcTemplate.query(match<String> { it.contains("SELECT") && it.contains("id = ?") }, any<RowMapper<WishlistSnapshotEntity>>(), eq(id)) } returns emptyList()
            repository.findById(id) shouldBe null
        }

        @Test
        fun `should handle null optional fields`() {
            val id = 1L
            every { jdbcTemplate.query(match<String> { it.contains("SELECT") && it.contains("id = ?") }, any<RowMapper<WishlistSnapshotEntity>>(), eq(id)) } answers {
                val rowMapper = secondArg<RowMapper<WishlistSnapshotEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, raiderId = null, teamId = null, seasonId = null, periodId = null), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.raiderId shouldBe null
            result?.teamId shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdTests {

        @Test
        fun `should return snapshots for raider`() {
            val raiderId = 100L
            every { jdbcTemplate.query(match<String> { it.contains("raider_id = ?") }, any<RowMapper<WishlistSnapshotEntity>>(), eq(raiderId), any<Int>(), any<Long>()) } answers {
                val rowMapper = secondArg<RowMapper<WishlistSnapshotEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, raiderId = raiderId), 0), rowMapper.mapRow(mockResultSet(2L, raiderId = raiderId), 1))
            }
            val result = repository.findByRaiderId(raiderId, 0L, 10)
            result.size shouldBe 2
        }
    }

    @Nested
    inner class FindByTeamIdTests {

        @Test
        fun `should return snapshots for team`() {
            val teamId = 100L
            every { jdbcTemplate.query(match<String> { it.contains("team_id = ?") }, any<RowMapper<WishlistSnapshotEntity>>(), eq(teamId), any<Int>(), any<Long>()) } answers {
                val rowMapper = secondArg<RowMapper<WishlistSnapshotEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, teamId = teamId), 0))
            }
            val result = repository.findByTeamId(teamId, 0L, 10)
            result.size shouldBe 1
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should insert new snapshot when id is null`() {
            val entity = createEntity(id = null)
            val generatedId = 1L
            every { jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>()) } answers {
                secondArg<GeneratedKeyHolder>().keyList.add(mapOf("id" to generatedId)); 1
            }
            val result = repository.save(entity)
            result.id shouldBe generatedId
        }

        @Test
        fun `should update existing snapshot when id is not null`() {
            val entity = createEntity(id = 1L)
            val sqlSlot = slot<String>()
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1
            repository.save(entity)
            sqlSlot.captured.contains("UPDATE") shouldBe true
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete snapshot by id`() {
            val id = 1L
            every { jdbcTemplate.update(match<String> { it.contains("DELETE") }, eq(id)) } returns 1
            repository.delete(id)
            verify { jdbcTemplate.update(match { it.contains("DELETE") }, id) }
        }
    }

    private fun mockResultSet(id: Long, raiderId: Long? = 100L, teamId: Long? = 1L, seasonId: Long? = 1L, periodId: Long? = 5L): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns (raiderId ?: 0L)
        every { rs.getLong("team_id") } returns (teamId ?: 0L)
        every { rs.getLong("season_id") } returns (seasonId ?: 0L)
        every { rs.getLong("period_id") } returns (periodId ?: 0L)
        every { rs.getString("character_name") } returns "TestChar"
        every { rs.getString("character_realm") } returns "Illidan"
        every { rs.getString("character_region") } returns "US"
        every { rs.getString("raw_payload") } returns "{}"
        var wasNullCount = 0
        every { rs.wasNull() } answers { val isNull = when(wasNullCount) { 0 -> raiderId == null; 1 -> teamId == null; 2 -> seasonId == null; 3 -> periodId == null; else -> false }; wasNullCount++; isNull }
        every { rs.getTimestamp("synced_at") } returns Timestamp.from(now.toInstant())
        return rs
    }

    private fun createEntity(id: Long? = 1L, raiderId: Long? = 100L, characterName: String = "TestChar", characterRealm: String = "Illidan", characterRegion: String? = "US", teamId: Long? = 1L, seasonId: Long? = 1L, periodId: Long? = 5L, rawPayload: String = "{}", syncedAt: OffsetDateTime = now) =
        WishlistSnapshotEntity(id, raiderId, characterName, characterRealm, characterRegion, teamId, seasonId, periodId, rawPayload, syncedAt)
}
