package com.edgerush.lootman.api.discord

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.domain.shared.DiscordUserLinkAlreadyExistsException
import com.edgerush.lootman.domain.shared.DiscordUserLinkNotFoundException
import com.edgerush.lootman.infrastructure.discord.InMemoryDiscordUserLinkRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for DiscordUserLinkCrudServiceImpl.
 *
 * Uses InMemoryDiscordUserLinkRepository for fast, isolated tests.
 */
class DiscordUserLinkCrudServiceImplTest : UnitTest() {
    private lateinit var repository: InMemoryDiscordUserLinkRepository
    private lateinit var service: DiscordUserLinkCrudServiceImpl

    private val discordUserId1 = "123456789012345678"
    private val discordUserId2 = "987654321098765432"
    private val raiderId1 = 1L
    private val raiderId2 = 2L

    @BeforeEach
    fun setup() {
        repository = InMemoryDiscordUserLinkRepository()
        service = DiscordUserLinkCrudServiceImpl(repository)
    }

    @Nested
    inner class Create {
        @Test
        fun `should create a new link`() {
            // Arrange
            val request =
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId1,
                    isPrimary = false,
                    linkedBy = "test-user",
                )

            // Act
            val response = service.create(request)

            // Assert
            response.discordUserId shouldBe discordUserId1
            response.raiderId shouldBe raiderId1
            response.linkedBy shouldBe "test-user"
        }

        @Test
        fun `should make first link primary automatically`() {
            // Arrange
            val request =
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId1,
                    isPrimary = false,
                )

            // Act
            val response = service.create(request)

            // Assert
            response.isPrimary shouldBe true
        }

        @Test
        fun `should not make second link primary automatically`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            val request =
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId2,
                    isPrimary = false,
                )

            // Act
            val response = service.create(request)

            // Assert
            response.isPrimary shouldBe false
        }

        @Test
        fun `should clear existing primary when creating new primary link`() {
            // Arrange
            val firstLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId1,
                        isPrimary = true,
                    ),
                )

            // Act
            service.create(
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId2,
                    isPrimary = true,
                ),
            )

            // Assert
            val updatedFirstLink = service.findById(firstLink.id)
            updatedFirstLink.isPrimary shouldBe false
        }

        @Test
        fun `should throw exception when link already exists`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            val request = CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1)

            // Act & Assert
            shouldThrow<DiscordUserLinkAlreadyExistsException> {
                service.create(request)
            }
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should find existing link`() {
            // Arrange
            val created =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId1,
                    ),
                )

            // Act
            val found = service.findById(created.id)

            // Assert
            found.id shouldBe created.id
            found.discordUserId shouldBe discordUserId1
        }

        @Test
        fun `should throw exception when link not found`() {
            // Act & Assert
            shouldThrow<DiscordUserLinkNotFoundException> {
                service.findById(999L)
            }
        }
    }

    @Nested
    inner class FindAll {
        @Test
        fun `should return paginated results`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId2, raiderId = raiderId2))
            val pageRequest = PageRequest(page = 0, size = 10)

            // Act
            val response = service.findAll(pageRequest)

            // Assert
            response.content shouldHaveSize 2
            response.totalElements shouldBe 2
            response.totalPages shouldBe 1
        }

        @Test
        fun `should handle empty results`() {
            // Arrange
            val pageRequest = PageRequest(page = 0, size = 10)

            // Act
            val response = service.findAll(pageRequest)

            // Assert
            response.content.shouldBeEmpty()
            response.totalElements shouldBe 0
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should update link to primary`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            val secondLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId2,
                    ),
                )

            // Act
            val updated = service.update(secondLink.id, UpdateDiscordUserLinkRequest(isPrimary = true))

            // Assert
            updated.isPrimary shouldBe true
        }

        @Test
        fun `should clear other primary when updating to primary`() {
            // Arrange
            val firstLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId1,
                        isPrimary = true,
                    ),
                )
            val secondLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId2,
                    ),
                )

            // Act
            service.update(secondLink.id, UpdateDiscordUserLinkRequest(isPrimary = true))

            // Assert
            val updatedFirst = service.findById(firstLink.id)
            updatedFirst.isPrimary shouldBe false
        }

        @Test
        fun `should throw exception when link not found`() {
            // Act & Assert
            shouldThrow<DiscordUserLinkNotFoundException> {
                service.update(999L, UpdateDiscordUserLinkRequest(isPrimary = true))
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete existing link`() {
            // Arrange
            val created =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId1,
                    ),
                )

            // Act
            service.delete(created.id)

            // Assert
            service.existsById(created.id) shouldBe false
        }

        @Test
        fun `should promote another link to primary when deleting primary`() {
            // Arrange
            val primaryLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId1,
                        isPrimary = true,
                    ),
                )
            val secondLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId2,
                    ),
                )

            // Act
            service.delete(primaryLink.id)

            // Assert
            val promoted = service.findById(secondLink.id)
            promoted.isPrimary shouldBe true
        }

        @Test
        fun `should throw exception when link not found`() {
            // Act & Assert
            shouldThrow<DiscordUserLinkNotFoundException> {
                service.delete(999L)
            }
        }
    }

    @Nested
    inner class FindByDiscordUserId {
        @Test
        fun `should find all links for Discord user`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId2))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val links = service.findByDiscordUserId(discordUserId1)

            // Assert
            links shouldHaveSize 2
            links.all { it.discordUserId == discordUserId1 } shouldBe true
        }

        @Test
        fun `should return empty list when no links exist`() {
            // Act
            val links = service.findByDiscordUserId(discordUserId1)

            // Assert
            links.shouldBeEmpty()
        }
    }

    @Nested
    inner class FindPrimaryByDiscordUserId {
        @Test
        fun `should find primary link`() {
            // Arrange
            service.create(
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId1,
                    isPrimary = true,
                ),
            )

            // Act
            val primary = service.findPrimaryByDiscordUserId(discordUserId1)

            // Assert
            primary.isPrimary shouldBe true
            primary.discordUserId shouldBe discordUserId1
        }

        @Test
        fun `should throw exception when no primary link exists`() {
            // Act & Assert
            shouldThrow<NoSuchElementException> {
                service.findPrimaryByDiscordUserId(discordUserId1)
            }
        }
    }

    @Nested
    inner class FindByRaiderId {
        @Test
        fun `should find all links for raider`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val links = service.findByRaiderId(raiderId1)

            // Assert
            links shouldHaveSize 2
            links.all { it.raiderId == raiderId1 } shouldBe true
        }
    }

    @Nested
    inner class SetPrimary {
        @Test
        fun `should set link as primary`() {
            // Arrange
            service.create(
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId1,
                    raiderId = raiderId1,
                    isPrimary = true,
                ),
            )
            val secondLink =
                service.create(
                    CreateDiscordUserLinkRequest(
                        discordUserId = discordUserId1,
                        raiderId = raiderId2,
                    ),
                )

            // Act
            val updated = service.setPrimary(secondLink.id)

            // Assert
            updated.isPrimary shouldBe true
        }

        @Test
        fun `should throw exception when link not found`() {
            // Act & Assert
            shouldThrow<DiscordUserLinkNotFoundException> {
                service.setPrimary(999L)
            }
        }
    }

    @Nested
    inner class DeleteByDiscordUserId {
        @Test
        fun `should delete all links for Discord user`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId2))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId2, raiderId = raiderId1))

            // Act
            val deletedCount = service.deleteByDiscordUserId(discordUserId1)

            // Assert
            deletedCount shouldBe 2
            service.findByDiscordUserId(discordUserId1).shouldBeEmpty()
            service.findByDiscordUserId(discordUserId2) shouldHaveSize 1
        }
    }

    @Nested
    inner class CountByDiscordUserId {
        @Test
        fun `should count links for Discord user`() {
            // Arrange
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId1))
            service.create(CreateDiscordUserLinkRequest(discordUserId = discordUserId1, raiderId = raiderId2))

            // Act
            val count = service.countByDiscordUserId(discordUserId1)

            // Assert
            count shouldBe 2
        }
    }
}
