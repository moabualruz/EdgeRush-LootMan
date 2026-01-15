package com.edgerush.lootman.infrastructure.auth

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for InMemoryUserRepository.
 */
class InMemoryUserRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryUserRepository

    private val discordId1 = "123456789012345678"
    private val discordId2 = "987654321098765432"
    private val battlenetId1 = "12345"
    private val guildId = GuildId("test-guild")

    @BeforeEach
    fun setup() {
        repository = InMemoryUserRepository()
    }

    private fun createUser(
        discordId: String? = discordId1,
        battlenetId: String? = null,
        username: String = "testuser",
    ): User =
        User(
            discordId = discordId,
            battlenetId = battlenetId,
            username = username,
        )

    @Nested
    inner class Save {
        @Test
        fun `should save user and assign ID`() {
            // Arrange
            val user = createUser()

            // Act
            val saved = repository.save(user)

            // Assert
            saved.id.shouldNotBeNull()
            saved.username shouldBe "testuser"
        }

        @Test
        fun `should update existing user`() {
            // Arrange
            val saved = repository.save(createUser())
            val updated = saved.updateProfile(username = "newname")

            // Act
            val result = repository.save(updated)

            // Assert
            result.id shouldBe saved.id
            result.username shouldBe "newname"
        }

        @Test
        fun `should generate unique IDs`() {
            // Arrange & Act
            val user1 = repository.save(createUser(discordId = discordId1))
            val user2 = repository.save(createUser(discordId = discordId2))

            // Assert
            user1.id shouldNotBe user2.id
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should find saved user`() {
            // Arrange
            val saved = repository.save(createUser())

            // Act
            val found = repository.findById(saved.id!!)

            // Assert
            found.shouldNotBeNull()
            found.id shouldBe saved.id
        }

        @Test
        fun `should return null for non-existent ID`() {
            // Act
            val found = repository.findById(UserId(999L))

            // Assert
            found.shouldBeNull()
        }
    }

    @Nested
    inner class FindByDiscordId {
        @Test
        fun `should find user by Discord ID`() {
            // Arrange
            repository.save(createUser(discordId = discordId1))

            // Act
            val found = repository.findByDiscordId(discordId1)

            // Assert
            found.shouldNotBeNull()
            found.discordId shouldBe discordId1
        }

        @Test
        fun `should return null when Discord ID not found`() {
            // Act
            val found = repository.findByDiscordId("nonexistent")

            // Assert
            found.shouldBeNull()
        }
    }

    @Nested
    inner class FindByBattlenetId {
        @Test
        fun `should find user by Battlenet ID`() {
            // Arrange
            repository.save(createUser(discordId = null, battlenetId = battlenetId1))

            // Act
            val found = repository.findByBattlenetId(battlenetId1)

            // Assert
            found.shouldNotBeNull()
            found.battlenetId shouldBe battlenetId1
        }

        @Test
        fun `should return null when Battlenet ID not found`() {
            // Act
            val found = repository.findByBattlenetId("nonexistent")

            // Assert
            found.shouldBeNull()
        }
    }

    @Nested
    inner class FindByGuildId {
        @Test
        fun `should find users in guild`() {
            // Arrange
            repository.save(createUser(discordId = discordId1).withGuild(guildId))
            repository.save(createUser(discordId = discordId2).withGuild(guildId))
            repository.save(createUser(discordId = "333", username = "other"))

            // Act
            val users = repository.findByGuildId(guildId)

            // Assert
            users shouldHaveSize 2
        }
    }

    @Nested
    inner class ExistsByDiscordId {
        @Test
        fun `should return true when Discord ID exists`() {
            // Arrange
            repository.save(createUser(discordId = discordId1))

            // Assert
            repository.existsByDiscordId(discordId1) shouldBe true
        }

        @Test
        fun `should return false when Discord ID does not exist`() {
            // Assert
            repository.existsByDiscordId("nonexistent") shouldBe false
        }
    }

    @Nested
    inner class ExistsByBattlenetId {
        @Test
        fun `should return true when Battlenet ID exists`() {
            // Arrange
            repository.save(createUser(discordId = null, battlenetId = battlenetId1))

            // Assert
            repository.existsByBattlenetId(battlenetId1) shouldBe true
        }

        @Test
        fun `should return false when Battlenet ID does not exist`() {
            // Assert
            repository.existsByBattlenetId("nonexistent") shouldBe false
        }
    }

    @Nested
    inner class DeleteById {
        @Test
        fun `should delete user`() {
            // Arrange
            val saved = repository.save(createUser())

            // Act
            repository.deleteById(saved.id!!)

            // Assert
            repository.findById(saved.id!!).shouldBeNull()
        }
    }

    @Nested
    inner class FindAllPaginated {
        @Test
        fun `should return paginated results`() {
            // Arrange
            repeat(5) { i ->
                repository.save(createUser(discordId = "user$i", username = "user$i"))
            }

            // Act
            val page1 = repository.findAll(offset = 0, limit = 2)
            val page2 = repository.findAll(offset = 2, limit = 2)

            // Assert
            page1 shouldHaveSize 2
            page2 shouldHaveSize 2
        }
    }

    @Nested
    inner class Count {
        @Test
        fun `should count all users`() {
            // Arrange
            repository.save(createUser(discordId = discordId1))
            repository.save(createUser(discordId = discordId2))

            // Assert
            repository.count() shouldBe 2
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `should clear all data`() {
            // Arrange
            repository.save(createUser())

            // Act
            repository.clear()

            // Assert
            repository.count() shouldBe 0
        }
    }
}
