package com.edgerush.lootman.infrastructure.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for InMemoryGuildRepository.
 *
 * Tests the in-memory storage and retrieval of guilds.
 */
class InMemoryGuildRepositoryTest : UnitTest() {

    private lateinit var repository: InMemoryGuildRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryGuildRepository()
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should save new guild`() {
            // Given
            val guild = createGuild(id = GuildId("test-guild-1"))

            // When
            val result = repository.save(guild)

            // Then
            result shouldBe guild
            repository.findById(GuildId("test-guild-1")) shouldNotBe null
        }

        @Test
        fun `should update existing guild`() {
            // Given
            val guildId = GuildId("update-guild")
            val originalGuild = createGuild(id = guildId, name = "Original Name")
            repository.save(originalGuild)

            val updatedGuild = createGuild(id = guildId, name = "Updated Name")

            // When
            val result = repository.save(updatedGuild)

            // Then
            result.name shouldBe "Updated Name"
            repository.findById(guildId)?.name shouldBe "Updated Name"
        }

        @Test
        fun `should return the saved guild`() {
            // Given
            val guild = createGuild(id = GuildId("return-guild"))

            // When
            val result = repository.save(guild)

            // Then
            result shouldBe guild
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return guild when found`() {
            // Given
            val guildId = GuildId("find-guild")
            val guild = createGuild(id = guildId, name = "Find Guild")
            repository.save(guild)

            // When
            val result = repository.findById(guildId)

            // Then
            result shouldNotBe null
            result?.id shouldBe guildId
            result?.name shouldBe "Find Guild"
        }

        @Test
        fun `should return null when guild not found`() {
            // When
            val result = repository.findById(GuildId("non-existent"))

            // Then
            result shouldBe null
        }

        @Test
        fun `should return correct guild when multiple exist`() {
            // Given
            val guild1 = createGuild(id = GuildId("guild-1"), name = "Guild One")
            val guild2 = createGuild(id = GuildId("guild-2"), name = "Guild Two")
            val guild3 = createGuild(id = GuildId("guild-3"), name = "Guild Three")
            repository.save(guild1)
            repository.save(guild2)
            repository.save(guild3)

            // When
            val result = repository.findById(GuildId("guild-2"))

            // Then
            result?.name shouldBe "Guild Two"
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return all guilds`() {
            // Given
            repository.save(createGuild(id = GuildId("all-1"), name = "Guild 1"))
            repository.save(createGuild(id = GuildId("all-2"), name = "Guild 2"))
            repository.save(createGuild(id = GuildId("all-3"), name = "Guild 3"))

            // When
            val result = repository.findAll()

            // Then
            result shouldHaveSize 3
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // When
            val result = repository.findAll()

            // Then
            result.shouldBeEmpty()
        }

        @Test
        fun `should return both active and inactive guilds`() {
            // Given
            repository.save(createGuild(id = GuildId("active-1"), isActive = true))
            repository.save(createGuild(id = GuildId("inactive-1"), isActive = false))
            repository.save(createGuild(id = GuildId("active-2"), isActive = true))

            // When
            val result = repository.findAll()

            // Then
            result shouldHaveSize 3
        }
    }

    @Nested
    inner class FindAllActiveTests {

        @Test
        fun `should return only active guilds`() {
            // Given
            repository.save(createGuild(id = GuildId("active-1"), isActive = true))
            repository.save(createGuild(id = GuildId("inactive-1"), isActive = false))
            repository.save(createGuild(id = GuildId("active-2"), isActive = true))
            repository.save(createGuild(id = GuildId("inactive-2"), isActive = false))

            // When
            val result = repository.findAllActive()

            // Then
            result shouldHaveSize 2
            result.all { it.isActive } shouldBe true
        }

        @Test
        fun `should return empty list when no active guilds exist`() {
            // Given
            repository.save(createGuild(id = GuildId("inactive-only"), isActive = false))

            // When
            val result = repository.findAllActive()

            // Then
            result.shouldBeEmpty()
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // When
            val result = repository.findAllActive()

            // Then
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class DeleteByIdTests {

        @Test
        fun `should return true when guild deleted`() {
            // Given
            val guildId = GuildId("delete-guild")
            repository.save(createGuild(id = guildId))

            // When
            val result = repository.deleteById(guildId)

            // Then
            result shouldBe true
            repository.findById(guildId) shouldBe null
        }

        @Test
        fun `should return false when guild not found`() {
            // When
            val result = repository.deleteById(GuildId("non-existent"))

            // Then
            result shouldBe false
        }

        @Test
        fun `should not affect other guilds when deleting`() {
            // Given
            repository.save(createGuild(id = GuildId("keep-1")))
            repository.save(createGuild(id = GuildId("delete-me")))
            repository.save(createGuild(id = GuildId("keep-2")))

            // When
            repository.deleteById(GuildId("delete-me"))

            // Then
            repository.findAll() shouldHaveSize 2
            repository.findById(GuildId("keep-1")) shouldNotBe null
            repository.findById(GuildId("keep-2")) shouldNotBe null
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when guild exists`() {
            // Given
            val guildId = GuildId("existing-guild")
            repository.save(createGuild(id = guildId))

            // When
            val result = repository.existsById(guildId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when guild does not exist`() {
            // When
            val result = repository.existsById(GuildId("non-existent"))

            // Then
            result shouldBe false
        }

        @Test
        fun `should return false after guild is deleted`() {
            // Given
            val guildId = GuildId("deleted-guild")
            repository.save(createGuild(id = guildId))
            repository.deleteById(guildId)

            // When
            val result = repository.existsById(guildId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `should remove all guilds`() {
            // Given
            repository.save(createGuild(id = GuildId("guild-1")))
            repository.save(createGuild(id = GuildId("guild-2")))
            repository.save(createGuild(id = GuildId("guild-3")))

            // When
            repository.clear()

            // Then
            repository.findAll().shouldBeEmpty()
        }

        @Test
        fun `should handle clearing empty repository`() {
            // When
            repository.clear()

            // Then
            repository.findAll().shouldBeEmpty()
        }
    }

    // Helper method

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
        updatedAt: Instant = Instant.now()
    ): Guild = Guild(
        id = id,
        name = name,
        description = description,
        realm = realm,
        region = region,
        settings = settings,
        syncStatus = syncStatus,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
