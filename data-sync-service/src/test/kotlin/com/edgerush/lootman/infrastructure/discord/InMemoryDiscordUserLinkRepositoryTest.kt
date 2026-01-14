package com.edgerush.lootman.infrastructure.discord

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for InMemoryDiscordUserLinkRepository.
 */
class InMemoryDiscordUserLinkRepositoryTest : UnitTest() {

    private lateinit var repository: InMemoryDiscordUserLinkRepository

    private val discordUserId1 = DiscordUserId("123456789012345678")
    private val discordUserId2 = DiscordUserId("987654321098765432")
    private val raiderId1 = RaiderId(1L)
    private val raiderId2 = RaiderId(2L)

    @BeforeEach
    fun setup() {
        repository = InMemoryDiscordUserLinkRepository()
    }

    private fun createLink(
        discordUserId: DiscordUserId = discordUserId1,
        raiderId: RaiderId = raiderId1,
        isPrimary: Boolean = false
    ): DiscordUserLink = DiscordUserLink.create(
        discordUserId = discordUserId,
        raiderId = raiderId,
        isPrimary = isPrimary,
        linkedBy = "test"
    )

    @Nested
    inner class Save {

        @Test
        fun `should save link and assign ID`() {
            // Arrange
            val link = createLink()

            // Act
            val saved = repository.save(link)

            // Assert
            saved.id.shouldNotBeNull()
            saved.discordUserId shouldBe discordUserId1
            saved.raiderId shouldBe raiderId1
        }

        @Test
        fun `should update existing link`() {
            // Arrange
            val saved = repository.save(createLink())
            val updated = saved.markAsPrimary()

            // Act
            val result = repository.save(updated)

            // Assert
            result.id shouldBe saved.id
            result.isPrimary shouldBe true
        }

        @Test
        fun `should generate unique IDs`() {
            // Arrange & Act
            val link1 = repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            val link2 = repository.save(createLink(discordUserId = discordUserId2, raiderId = raiderId2))

            // Assert
            link1.id shouldNotBe link2.id
        }
    }

    @Nested
    inner class FindById {

        @Test
        fun `should find saved link`() {
            // Arrange
            val saved = repository.save(createLink())

            // Act
            val found = repository.findById(saved.id!!)

            // Assert
            found.shouldNotBeNull()
            found.id shouldBe saved.id
        }

        @Test
        fun `should return null for non-existent ID`() {
            // Act
            val found = repository.findById(DiscordUserLinkId(999L))

            // Assert
            found.shouldBeNull()
        }
    }

    @Nested
    inner class FindByDiscordUserId {

        @Test
        fun `should find all links for Discord user`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId2))
            repository.save(createLink(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val links = repository.findByDiscordUserId(discordUserId1)

            // Assert
            links shouldHaveSize 2
        }

        @Test
        fun `should return primary link first`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1, isPrimary = false))
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId2, isPrimary = true))

            // Act
            val links = repository.findByDiscordUserId(discordUserId1)

            // Assert
            links.first().isPrimary shouldBe true
        }
    }

    @Nested
    inner class FindPrimaryByDiscordUserId {

        @Test
        fun `should find primary link`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1, isPrimary = false))
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId2, isPrimary = true))

            // Act
            val primary = repository.findPrimaryByDiscordUserId(discordUserId1)

            // Assert
            primary.shouldNotBeNull()
            primary.isPrimary shouldBe true
            primary.raiderId shouldBe raiderId2
        }

        @Test
        fun `should return null when no primary exists`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, isPrimary = false))

            // Act
            val primary = repository.findPrimaryByDiscordUserId(discordUserId1)

            // Assert
            primary.shouldBeNull()
        }
    }

    @Nested
    inner class FindByRaiderId {

        @Test
        fun `should find all links for raider`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            repository.save(createLink(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val links = repository.findByRaiderId(raiderId1)

            // Assert
            links shouldHaveSize 2
        }
    }

    @Nested
    inner class ExistsByDiscordUserIdAndRaiderId {

        @Test
        fun `should return true when link exists`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))

            // Act
            val exists = repository.existsByDiscordUserIdAndRaiderId(discordUserId1, raiderId1)

            // Assert
            exists shouldBe true
        }

        @Test
        fun `should return false when link does not exist`() {
            // Act
            val exists = repository.existsByDiscordUserIdAndRaiderId(discordUserId1, raiderId1)

            // Assert
            exists shouldBe false
        }
    }

    @Nested
    inner class DeleteById {

        @Test
        fun `should delete link`() {
            // Arrange
            val saved = repository.save(createLink())

            // Act
            repository.deleteById(saved.id!!)

            // Assert
            repository.findById(saved.id!!).shouldBeNull()
        }
    }

    @Nested
    inner class DeleteByDiscordUserId {

        @Test
        fun `should delete all links for Discord user`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId2))
            repository.save(createLink(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val deletedCount = repository.deleteByDiscordUserId(discordUserId1)

            // Assert
            deletedCount shouldBe 2
            repository.findByDiscordUserId(discordUserId1).shouldBeEmpty()
            repository.findByDiscordUserId(discordUserId2) shouldHaveSize 1
        }
    }

    @Nested
    inner class ClearPrimaryForDiscordUser {

        @Test
        fun `should clear primary flag`() {
            // Arrange
            val saved = repository.save(createLink(discordUserId = discordUserId1, isPrimary = true))

            // Act
            repository.clearPrimaryForDiscordUser(discordUserId1)

            // Assert
            val updated = repository.findById(saved.id!!)
            updated!!.isPrimary shouldBe false
        }

        @Test
        fun `should not affect other users`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, isPrimary = true))
            val otherUser = repository.save(createLink(discordUserId = discordUserId2, isPrimary = true))

            // Act
            repository.clearPrimaryForDiscordUser(discordUserId1)

            // Assert
            val otherUpdated = repository.findById(otherUser.id!!)
            otherUpdated!!.isPrimary shouldBe true
        }
    }

    @Nested
    inner class CountByDiscordUserId {

        @Test
        fun `should count links for Discord user`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId2))

            // Act
            val count = repository.countByDiscordUserId(discordUserId1)

            // Assert
            count shouldBe 2
        }
    }

    @Nested
    inner class FindAllPaginated {

        @Test
        fun `should return paginated results`() {
            // Arrange
            repeat(5) { i ->
                repository.save(createLink(
                    discordUserId = DiscordUserId("12345678901234567${i}"),
                    raiderId = RaiderId(i.toLong() + 1)
                ))
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
        fun `should count all links`() {
            // Arrange
            repository.save(createLink(discordUserId = discordUserId1, raiderId = raiderId1))
            repository.save(createLink(discordUserId = discordUserId2, raiderId = raiderId2))

            // Act
            val count = repository.count()

            // Assert
            count shouldBe 2
        }
    }

    @Nested
    inner class Clear {

        @Test
        fun `should clear all data`() {
            // Arrange
            repository.save(createLink())

            // Act
            repository.clear()

            // Assert
            repository.count() shouldBe 0
        }
    }
}
