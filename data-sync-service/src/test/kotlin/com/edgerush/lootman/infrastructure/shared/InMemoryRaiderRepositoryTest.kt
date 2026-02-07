package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.datasync.test.fixtures.RaiderFixtures
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for InMemoryRaiderRepository.
 *
 * Following TDD - these tests are written before the implementation.
 */
class InMemoryRaiderRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryRaiderRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryRaiderRepository()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save and return the raider`() {
            // Arrange
            val raider = createRaider()

            // Act
            val saved = repository.save(raider)

            // Assert
            saved shouldBe raider
        }

        @Test
        fun `should persist raider to storage`() {
            // Arrange
            val raider = createRaider()

            // Act
            repository.save(raider)
            val retrieved = repository.findById(raider.id)

            // Assert
            retrieved shouldBe raider
        }

        @Test
        fun `should overwrite existing raider when saving with same id`() {
            // Arrange
            val originalRaider = createRaider(status = RaiderStatus.ACTIVE)
            repository.save(originalRaider)

            val modifiedRaider =
                originalRaider.copy(
                    status = RaiderStatus.BENCHED,
                    rank = "Officer",
                )

            // Act
            repository.save(modifiedRaider)
            val retrieved = repository.findById(originalRaider.id)

            // Assert
            retrieved shouldBe modifiedRaider
            retrieved?.status shouldBe RaiderStatus.BENCHED
            retrieved?.rank shouldBe "Officer"
        }

        @Test
        fun `should save multiple raiders with different ids`() {
            // Arrange
            val raider1 = createRaider(id = RaiderId(1L), characterName = "Raider1")
            val raider2 = createRaider(id = RaiderId(2L), characterName = "Raider2")
            val raider3 = createRaider(id = RaiderId(3L), characterName = "Raider3")

            // Act
            repository.save(raider1)
            repository.save(raider2)
            repository.save(raider3)

            // Assert
            repository.findById(raider1.id) shouldBe raider1
            repository.findById(raider2.id) shouldBe raider2
            repository.findById(raider3.id) shouldBe raider3
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return raider when found`() {
            // Arrange
            val raider = createRaider()
            repository.save(raider)

            // Act
            val retrieved = repository.findById(raider.id)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe raider
        }

        @Test
        fun `should return null when raider not found`() {
            // Arrange
            val nonExistentId = RaiderId(9999L)

            // Act
            val retrieved = repository.findById(nonExistentId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null for id that was never saved`() {
            // Arrange
            val raider = createRaider(id = RaiderId(1L))
            val differentId = RaiderId(2L)
            repository.save(raider)

            // Act
            val retrieved = repository.findById(differentId)

            // Assert
            retrieved shouldBe null
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return all raiders for a specific guild`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raider1 = createRaider(id = RaiderId(1L), guildId = guildId, characterName = "Raider1")
            val raider2 = createRaider(id = RaiderId(2L), guildId = guildId, characterName = "Raider2")
            val raider3 = createRaider(id = RaiderId(3L), guildId = GuildId("other-guild"), characterName = "Raider3")

            repository.save(raider1)
            repository.save(raider2)
            repository.save(raider3)

            // Act
            val results = repository.findByGuildId(guildId)

            // Assert
            results shouldHaveSize 2
            results shouldContainExactlyInAnyOrder listOf(raider1, raider2)
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // Arrange
            val guildWithRaiders = GuildId("guild-with-raiders")
            val guildWithoutRaiders = GuildId("guild-without-raiders")
            val raider = createRaider(guildId = guildWithRaiders)
            repository.save(raider)

            // Act
            val results = repository.findByGuildId(guildWithoutRaiders)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should return empty list when repository is empty`() {
            // Arrange
            val guildId = GuildId("test-guild")

            // Act
            val results = repository.findByGuildId(guildId)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should handle multiple guilds with raiders correctly`() {
            // Arrange
            val guild1 = GuildId("guild-1")
            val guild2 = GuildId("guild-2")
            val guild3 = GuildId("guild-3")

            val raider1 = createRaider(id = RaiderId(1L), guildId = guild1, characterName = "R1")
            val raider2 = createRaider(id = RaiderId(2L), guildId = guild1, characterName = "R2")
            val raider3 = createRaider(id = RaiderId(3L), guildId = guild2, characterName = "R3")
            val raider4 = createRaider(id = RaiderId(4L), guildId = guild3, characterName = "R4")
            val raider5 = createRaider(id = RaiderId(5L), guildId = guild3, characterName = "R5")
            val raider6 = createRaider(id = RaiderId(6L), guildId = guild3, characterName = "R6")

            repository.save(raider1)
            repository.save(raider2)
            repository.save(raider3)
            repository.save(raider4)
            repository.save(raider5)
            repository.save(raider6)

            // Act & Assert
            repository.findByGuildId(guild1) shouldHaveSize 2
            repository.findByGuildId(guild2) shouldHaveSize 1
            repository.findByGuildId(guild3) shouldHaveSize 3
        }
    }

    @Nested
    inner class FindByGuildIdPaginatedTests {
        @Test
        fun `should return paginated results with offset and limit`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiders =
                (1..10).map { index ->
                    createRaider(id = RaiderId(index.toLong()), guildId = guildId, characterName = "Raider$index")
                }
            raiders.forEach { repository.save(it) }

            // Act
            val results = repository.findByGuildId(guildId, offset = 2, limit = 3)

            // Assert
            results shouldHaveSize 3
        }

        @Test
        fun `should return empty list when offset exceeds total count`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raider = createRaider(guildId = guildId)
            repository.save(raider)

            // Act
            val results = repository.findByGuildId(guildId, offset = 10, limit = 5)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should return remaining items when limit exceeds available`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiders =
                (1..5).map { index ->
                    createRaider(id = RaiderId(index.toLong()), guildId = guildId, characterName = "Raider$index")
                }
            raiders.forEach { repository.save(it) }

            // Act
            val results = repository.findByGuildId(guildId, offset = 3, limit = 10)

            // Assert
            results shouldHaveSize 2
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // Arrange
            val guildId = GuildId("empty-guild")

            // Act
            val results = repository.findByGuildId(guildId, offset = 0, limit = 10)

            // Assert
            results.shouldBeEmpty()
        }
    }

    @Nested
    inner class CountByGuildIdTests {
        @Test
        fun `should return correct count for guild with raiders`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raiders =
                (1..5).map { index ->
                    createRaider(id = RaiderId(index.toLong()), guildId = guildId, characterName = "Raider$index")
                }
            raiders.forEach { repository.save(it) }

            // Act
            val count = repository.countByGuildId(guildId)

            // Assert
            count shouldBe 5L
        }

        @Test
        fun `should return zero for guild with no raiders`() {
            // Arrange
            val guildId = GuildId("empty-guild")

            // Act
            val count = repository.countByGuildId(guildId)

            // Assert
            count shouldBe 0L
        }

        @Test
        fun `should only count raiders for specific guild`() {
            // Arrange
            val guild1 = GuildId("guild-1")
            val guild2 = GuildId("guild-2")
            val raiders1 =
                (1..3).map { index ->
                    createRaider(id = RaiderId(index.toLong()), guildId = guild1, characterName = "G1Raider$index")
                }
            val raiders2 =
                (4..6).map { index ->
                    createRaider(id = RaiderId(index.toLong()), guildId = guild2, characterName = "G2Raider$index")
                }
            raiders1.forEach { repository.save(it) }
            raiders2.forEach { repository.save(it) }

            // Act
            val count1 = repository.countByGuildId(guild1)
            val count2 = repository.countByGuildId(guild2)

            // Assert
            count1 shouldBe 3L
            count2 shouldBe 3L
        }
    }

    @Nested
    inner class FindByCharacterNameAndRealmTests {
        @Test
        fun `should return raider when found by name and realm`() {
            // Arrange
            val raider = createRaider(characterName = "Testchar", realm = "TestRealm")
            repository.save(raider)

            // Act
            val retrieved = repository.findByCharacterNameAndRealm("Testchar", "TestRealm")

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe raider
        }

        @Test
        fun `should return null when character name matches but realm differs`() {
            // Arrange
            val raider = createRaider(characterName = "Testchar", realm = "TestRealm")
            repository.save(raider)

            // Act
            val retrieved = repository.findByCharacterNameAndRealm("Testchar", "OtherRealm")

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null when realm matches but character name differs`() {
            // Arrange
            val raider = createRaider(characterName = "Testchar", realm = "TestRealm")
            repository.save(raider)

            // Act
            val retrieved = repository.findByCharacterNameAndRealm("OtherChar", "TestRealm")

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null when neither matches`() {
            // Arrange
            val raider = createRaider(characterName = "Testchar", realm = "TestRealm")
            repository.save(raider)

            // Act
            val retrieved = repository.findByCharacterNameAndRealm("OtherChar", "OtherRealm")

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null when repository is empty`() {
            // Act
            val retrieved = repository.findByCharacterNameAndRealm("Testchar", "TestRealm")

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should find correct raider among multiple with different names and realms`() {
            // Arrange
            val raider1 = createRaider(id = RaiderId(1L), characterName = "Char1", realm = "Realm1")
            val raider2 = createRaider(id = RaiderId(2L), characterName = "Char2", realm = "Realm1")
            val raider3 = createRaider(id = RaiderId(3L), characterName = "Char1", realm = "Realm2")
            val raider4 = createRaider(id = RaiderId(4L), characterName = "Char2", realm = "Realm2")

            repository.save(raider1)
            repository.save(raider2)
            repository.save(raider3)
            repository.save(raider4)

            // Act & Assert
            repository.findByCharacterNameAndRealm("Char1", "Realm1") shouldBe raider1
            repository.findByCharacterNameAndRealm("Char2", "Realm1") shouldBe raider2
            repository.findByCharacterNameAndRealm("Char1", "Realm2") shouldBe raider3
            repository.findByCharacterNameAndRealm("Char2", "Realm2") shouldBe raider4
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete existing raider`() {
            // Arrange
            val raider = createRaider()
            repository.save(raider)

            // Act
            repository.delete(raider.id)

            // Assert
            repository.findById(raider.id) shouldBe null
        }

        @Test
        fun `should not throw when deleting non-existent raider`() {
            // Arrange
            val nonExistentId = RaiderId(9999L)

            // Act & Assert - should not throw
            repository.delete(nonExistentId)
        }

        @Test
        fun `should only delete specified raider and leave others intact`() {
            // Arrange
            val raider1 = createRaider(id = RaiderId(1L), characterName = "Raider1")
            val raider2 = createRaider(id = RaiderId(2L), characterName = "Raider2")
            val raider3 = createRaider(id = RaiderId(3L), characterName = "Raider3")

            repository.save(raider1)
            repository.save(raider2)
            repository.save(raider3)

            // Act
            repository.delete(raider2.id)

            // Assert
            repository.findById(raider1.id) shouldBe raider1
            repository.findById(raider2.id) shouldBe null
            repository.findById(raider3.id) shouldBe raider3
        }

        @Test
        fun `should remove raider from guild query results after deletion`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val raider1 = createRaider(id = RaiderId(1L), guildId = guildId, characterName = "Raider1")
            val raider2 = createRaider(id = RaiderId(2L), guildId = guildId, characterName = "Raider2")

            repository.save(raider1)
            repository.save(raider2)

            // Act
            repository.delete(raider1.id)
            val results = repository.findByGuildId(guildId)

            // Assert
            results shouldHaveSize 1
            results shouldContain raider2
        }

        @Test
        fun `should remove raider from name-realm query after deletion`() {
            // Arrange
            val raider = createRaider(characterName = "Testchar", realm = "TestRealm")
            repository.save(raider)

            // Act
            repository.delete(raider.id)
            val retrieved = repository.findByCharacterNameAndRealm("Testchar", "TestRealm")

            // Assert
            retrieved shouldBe null
        }
    }

    @Nested
    inner class ConcurrencyTests {
        @Test
        fun `should handle concurrent saves without data loss`() {
            // Arrange
            val raiders =
                (1..100).map { index ->
                    createRaider(
                        id = RaiderId(index.toLong()),
                        characterName = "Raider$index",
                    )
                }

            // Act - simulate concurrent saves
            raiders.parallelStream().forEach { raider ->
                repository.save(raider)
            }

            // Assert - all raiders should be saved
            raiders.forEach { raider ->
                repository.findById(raider.id) shouldBe raider
            }
        }

        @Test
        fun `should handle concurrent reads and writes`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val initialRaiders =
                (1..50).map { index ->
                    createRaider(
                        id = RaiderId(index.toLong()),
                        guildId = guildId,
                        characterName = "Raider$index",
                    )
                }
            initialRaiders.forEach { repository.save(it) }

            // Act - concurrent reads while writing
            val newRaiders =
                (51..100).map { index ->
                    createRaider(
                        id = RaiderId(index.toLong()),
                        guildId = guildId,
                        characterName = "Raider$index",
                    )
                }

            newRaiders.parallelStream().forEach { raider ->
                repository.save(raider)
                repository.findByGuildId(guildId) // concurrent read
            }

            // Assert
            val allResults = repository.findByGuildId(guildId)
            allResults shouldHaveSize 100
        }
    }

    @Nested
    inner class RaiderStatusTests {
        @Test
        fun `should correctly store and retrieve raiders with different statuses`() {
            // Arrange
            val activeRaider = createRaider(id = RaiderId(1L), status = RaiderStatus.ACTIVE, characterName = "Active")
            val inactiveRaider = createRaider(id = RaiderId(2L), status = RaiderStatus.INACTIVE, characterName = "Inactive")
            val benchedRaider = createRaider(id = RaiderId(3L), status = RaiderStatus.BENCHED, characterName = "Benched")
            val trialRaider = createRaider(id = RaiderId(4L), status = RaiderStatus.TRIAL, characterName = "Trial")
            val alumniRaider = createRaider(id = RaiderId(5L), status = RaiderStatus.ALUMNI, characterName = "Alumni")

            // Act
            repository.save(activeRaider)
            repository.save(inactiveRaider)
            repository.save(benchedRaider)
            repository.save(trialRaider)
            repository.save(alumniRaider)

            // Assert
            repository.findById(activeRaider.id)?.status shouldBe RaiderStatus.ACTIVE
            repository.findById(inactiveRaider.id)?.status shouldBe RaiderStatus.INACTIVE
            repository.findById(benchedRaider.id)?.status shouldBe RaiderStatus.BENCHED
            repository.findById(trialRaider.id)?.status shouldBe RaiderStatus.TRIAL
            repository.findById(alumniRaider.id)?.status shouldBe RaiderStatus.ALUMNI
        }
    }

    @Nested
    inner class CharacterClassAndRoleTests {
        @Test
        fun `should correctly store and retrieve raiders with different classes and roles`() {
            // Arrange
            val tank =
                createRaider(
                    id = RaiderId(1L),
                    characterClass = CharacterClass.WARRIOR,
                    role = Role.TANK,
                    characterName = "Tank",
                )
            val healer =
                createRaider(
                    id = RaiderId(2L),
                    characterClass = CharacterClass.PRIEST,
                    role = Role.HEALER,
                    characterName = "Healer",
                )
            val dps =
                createRaider(
                    id = RaiderId(3L),
                    characterClass = CharacterClass.MAGE,
                    role = Role.DPS,
                    characterName = "DPS",
                )

            // Act
            repository.save(tank)
            repository.save(healer)
            repository.save(dps)

            // Assert
            val retrievedTank = repository.findById(tank.id)
            retrievedTank?.characterClass shouldBe CharacterClass.WARRIOR
            retrievedTank?.role shouldBe Role.TANK

            val retrievedHealer = repository.findById(healer.id)
            retrievedHealer?.characterClass shouldBe CharacterClass.PRIEST
            retrievedHealer?.role shouldBe Role.HEALER

            val retrievedDps = repository.findById(dps.id)
            retrievedDps?.characterClass shouldBe CharacterClass.MAGE
            retrievedDps?.role shouldBe Role.DPS
        }
    }

    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.now(),
        wowauditId: Long? = null,
    ): Raider =
        RaiderFixtures.createRaider(
            id = id,
            guildId = guildId,
            name = characterName,
            realm = realm,
            characterClass = characterClass,
            role = role,
            rank = rank,
            status = status,
            joinDate = joinDate,
            wowauditId = wowauditId,
        )
}
