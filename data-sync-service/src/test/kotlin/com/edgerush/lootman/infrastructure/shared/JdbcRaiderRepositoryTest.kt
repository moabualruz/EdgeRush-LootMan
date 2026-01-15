package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
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
import java.time.LocalDateTime

/**
 * Unit tests for JdbcRaiderRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raiders table.
 */
class JdbcRaiderRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return raider when found by id`() {
            // Given
            val raiderId = RaiderId(1L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(rowMapper.mapRow(mockResultSet(raiderId.value, "TestRaider", "Area52"), 0))
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result shouldNotBe null
            result?.id shouldBe raiderId
            result?.characterName shouldBe "TestRaider"
            result?.realm shouldBe "Area52"
        }

        @Test
        fun `should return null when raider not found`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findById(raiderId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val raiderId = RaiderId(42L)
            val joinDate = LocalDateTime.of(2024, 1, 15, 10, 0)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                val rs =
                    mockResultSet(
                        id = raiderId.value,
                        characterName = "FullRaider",
                        realm = "Illidan",
                        guildId = "test-guild",
                        characterClass = "WARRIOR",
                        role = "TANK",
                        rank = "Officer",
                        status = "ACTIVE",
                        joinDate = joinDate,
                        wowauditId = 12345L,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe 42L
            result?.characterName shouldBe "FullRaider"
            result?.realm shouldBe "Illidan"
            result?.guildId?.value shouldBe "test-guild"
            result?.characterClass shouldBe CharacterClass.WARRIOR
            result?.role shouldBe Role.TANK
            result?.rank shouldBe "Officer"
            result?.status shouldBe RaiderStatus.ACTIVE
            result?.joinDate shouldBe joinDate
            result?.wowauditId shouldBe 12345L
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return all raiders for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, "Raider1", "Area52", guildId.value), 0),
                    rowMapper.mapRow(mockResultSet(2L, "Raider2", "Area52", guildId.value), 1),
                    rowMapper.mapRow(mockResultSet(3L, "Raider3", "Area52", guildId.value), 2),
                )
            }

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result.size shouldBe 3
            result[0].characterName shouldBe "Raider1"
            result[1].characterName shouldBe "Raider2"
            result[2].characterName shouldBe "Raider3"
            result.all { it.guildId == guildId } shouldBe true
        }

        @Test
        fun `should return empty list when no raiders in guild`() {
            // Given
            val guildId = GuildId("empty-guild")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(guildId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByCharacterNameAndRealmTests {
        @Test
        fun `should return raider when found by name and realm`() {
            // Given
            val characterName = "UniqueRaider"
            val realm = "Illidan"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("characterName = ?") && it.contains("realm = ?") },
                    any<RowMapper<Raider>>(),
                    eq(characterName),
                    eq(realm),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, characterName, realm), 0))
            }

            // When
            val result = repository.findByCharacterNameAndRealm(characterName, realm)

            // Then
            result shouldNotBe null
            result?.characterName shouldBe characterName
            result?.realm shouldBe realm
        }

        @Test
        fun `should return null when raider not found by name and realm`() {
            // Given
            val characterName = "NonExistent"
            val realm = "Unknown"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("characterName = ?") && it.contains("realm = ?") },
                    any<RowMapper<Raider>>(),
                    eq(characterName),
                    eq(realm),
                )
            } returns emptyList()

            // When
            val result = repository.findByCharacterNameAndRealm(characterName, realm)

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindByGuildIdPaginatedTests {
        @Test
        fun `should return paginated raiders for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<Raider>>(),
                    eq(guildId.value),
                    eq(limit),
                    eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(11L, "Raider11", "Area52", guildId.value), 0),
                    rowMapper.mapRow(mockResultSet(12L, "Raider12", "Area52", guildId.value), 1),
                )
            }

            // When
            val result = repository.findByGuildId(guildId, offset, limit)

            // Then
            result.size shouldBe 2
            result[0].id.value shouldBe 11L
            result[1].id.value shouldBe 12L
        }

        @Test
        fun `should return empty list when no raiders in page`() {
            // Given
            val guildId = GuildId("test-guild")
            val offset = 1000L
            val limit = 10

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<Raider>>(),
                    eq(guildId.value),
                    eq(limit),
                    eq(offset),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(guildId, offset, limit)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class CountByGuildIdTests {
        @Test
        fun `should return count of raiders for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value),
                )
            } returns 42L

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 42L
        }

        @Test
        fun `should return zero when no raiders in guild`() {
            // Given
            val guildId = GuildId("empty-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value),
                )
            } returns 0L

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 0L
        }

        @Test
        fun `should return zero when query returns null`() {
            // Given
            val guildId = GuildId("null-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value),
                )
            } returns null

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 0L
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new raider when not exists`() {
            // Given
            val raider = createRaider(id = RaiderId(1L)) // new raider
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, raider.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(raider)

            // Then
            result shouldBe raider
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should update existing raider when exists`() {
            // Given
            val raider = createRaider(id = RaiderId(1L), characterName = "UpdatedName")
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, raider.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(raider)

            // Then
            result shouldBe raider
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should insert raider with non-null joinDate`() {
            // Given - covers the ?.let branch when joinDate is not null in insertRaider
            val joinDate = LocalDateTime.of(2024, 6, 15, 10, 30)
            val raider = createRaider(id = RaiderId(10L), joinDate = joinDate)

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, raider.id.value) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(raider)

            // Then
            result shouldBe raider
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    raider.guildId.value,
                    raider.characterName,
                    raider.realm,
                    raider.characterClass.name,
                    raider.role.name,
                    raider.rank,
                    raider.status.name,
                    match<Timestamp> { it.toLocalDateTime() == joinDate },
                    raider.wowauditId,
                )
            }
        }

        @Test
        fun `should update raider with non-null joinDate`() {
            // Given - covers the ?.let branch when joinDate is not null in updateRaider
            val joinDate = LocalDateTime.of(2024, 8, 20, 14, 45)
            val raider = createRaider(id = RaiderId(20L), joinDate = joinDate)

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, raider.id.value) } returns 1
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(raider)

            // Then
            result shouldBe raider
            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    raider.guildId.value,
                    raider.characterName,
                    raider.realm,
                    raider.characterClass.name,
                    raider.role.name,
                    raider.rank,
                    raider.status.name,
                    match<Timestamp> { it.toLocalDateTime() == joinDate },
                    raider.wowauditId,
                    raider.id.value,
                )
            }
        }

        @Test
        fun `should insert new raider when existsById returns null`() {
            // Given - covers the elvis branch when queryForObject returns null
            val raider = createRaider(id = RaiderId(30L))

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, raider.id.value) } returns null
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(raider)

            // Then
            result shouldBe raider
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete raider by id`() {
            // Given
            val raiderId = RaiderId(1L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(raiderId.value),
                )
            } returns 1

            // When
            repository.delete(raiderId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    raiderId.value,
                )
            }
        }

        @Test
        fun `should not throw when deleting non-existent raider`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(raiderId.value),
                )
            } returns 0

            // When/Then - should not throw
            repository.delete(raiderId)

            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") },
                    raiderId.value,
                )
            }
        }
    }

    @Nested
    inner class RowMapperEdgeCases {
        @Test
        fun `should default to WARRIOR when characterClass is invalid`() {
            // Given
            val raiderId = RaiderId(1L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            characterClass = "INVALID_CLASS",
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.characterClass shouldBe CharacterClass.WARRIOR
        }

        @Test
        fun `should default to DPS when role is invalid`() {
            // Given
            val raiderId = RaiderId(2L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            role = "INVALID_ROLE",
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.role shouldBe Role.DPS
        }

        @Test
        fun `should default to ACTIVE when status is invalid`() {
            // Given
            val raiderId = RaiderId(3L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            status = "INVALID_STATUS",
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.status shouldBe RaiderStatus.ACTIVE
        }

        @Test
        fun `should handle null characterClass`() {
            // Given
            val raiderId = RaiderId(4L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            characterClass = null,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.characterClass shouldBe CharacterClass.WARRIOR
        }

        @Test
        fun `should handle null role`() {
            // Given
            val raiderId = RaiderId(5L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            role = null,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.role shouldBe Role.DPS
        }

        @Test
        fun `should handle null status`() {
            // Given
            val raiderId = RaiderId(6L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            status = null,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.status shouldBe RaiderStatus.ACTIVE
        }

        @Test
        fun `should handle null guildId with default value`() {
            // Given
            val raiderId = RaiderId(7L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            guildId = null,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.guildId?.value shouldBe "default"
        }

        @Test
        fun `should handle class name with spaces`() {
            // Given
            val raiderId = RaiderId(8L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<Raider>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Raider>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = raiderId.value,
                            characterName = "TestRaider",
                            realm = "Area52",
                            characterClass = "death knight",
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(raiderId)

            // Then
            result?.characterClass shouldBe CharacterClass.DEATH_KNIGHT
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        characterName: String,
        realm: String,
        guildId: String? = "default-guild",
        characterClass: String? = "WARRIOR",
        role: String? = "DPS",
        rank: String? = null,
        status: String? = "ACTIVE",
        joinDate: LocalDateTime? = null,
        wowauditId: Long? = null,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getString("characterName") } returns characterName
        every { rs.getString("realm") } returns realm
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("characterClass") } returns characterClass
        every { rs.getString("role") } returns role
        every { rs.getString("rank") } returns rank
        every { rs.getString("status") } returns status
        every { rs.getTimestamp("joinDate") } returns joinDate?.let { Timestamp.valueOf(it) }
        every { rs.getLong("wowauditId") } returns (wowauditId ?: 0L)
        every { rs.wasNull() } returns (wowauditId == null)
        return rs
    }

    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        characterName: String = "TestRaider",
        realm: String = "Area52",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        rank: String? = null,
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = null,
        wowauditId: Long? = null,
    ): Raider =
        Raider(
            id = id,
            guildId = guildId,
            characterName = characterName,
            realm = realm,
            characterClass = characterClass,
            role = role,
            rank = rank,
            status = status,
            joinDate = joinDate,
            wowauditId = wowauditId,
        )
}
