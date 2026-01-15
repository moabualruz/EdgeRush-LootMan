package com.edgerush.lootman.api.discord

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.domain.shared.DiscordUserLinkAlreadyExistsException
import com.edgerush.lootman.domain.shared.DiscordUserLinkNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Unit tests for DiscordUserLinkController.
 */
class DiscordUserLinkControllerTest : UnitTest() {
    private lateinit var discordUserLinkService: DiscordUserLinkCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: DiscordUserLinkController

    private val discordUserId = "123456789012345678"
    private val raiderId = 1L

    @BeforeEach
    fun setup() {
        discordUserLinkService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = DiscordUserLinkController(discordUserLinkService, paginationProperties)
    }

    private fun createResponse(
        id: Long = 1L,
        discordUserId: String = this.discordUserId,
        raiderId: Long = this.raiderId,
        isPrimary: Boolean = false,
    ) = DiscordUserLinkResponse(
        id = id,
        discordUserId = discordUserId,
        raiderId = raiderId,
        isPrimary = isPrimary,
        linkedAt = Instant.now(),
        linkedBy = "test",
    )

    @Nested
    inner class FindAll {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { discordUserLinkService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                discordUserLinkService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<DiscordUserLinkResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { discordUserLinkService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should return link when found`() {
            // Given
            val expected = createResponse(id = 123L)
            every { discordUserLinkService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            verify(exactly = 1) { discordUserLinkService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { discordUserLinkService.findById(999L) } throws DiscordUserLinkNotFoundException(999L)

            // When & Then
            shouldThrow<DiscordUserLinkNotFoundException> {
                controller.findById(999L)
            }
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `should create link and return 201 status`() {
            // Given
            val request =
                CreateDiscordUserLinkRequest(
                    discordUserId = discordUserId,
                    raiderId = raiderId,
                    isPrimary = false,
                )
            val expected = createResponse(id = 1L)
            every { discordUserLinkService.create(request) } returns expected

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            verify(exactly = 1) { discordUserLinkService.create(request) }
        }

        @Test
        fun `should propagate exception when link already exists`() {
            // Given
            val request = CreateDiscordUserLinkRequest(discordUserId = discordUserId, raiderId = raiderId)
            every { discordUserLinkService.create(request) } throws DiscordUserLinkAlreadyExistsException(discordUserId, raiderId)

            // When & Then
            shouldThrow<DiscordUserLinkAlreadyExistsException> {
                controller.create(request)
            }
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should update link`() {
            // Given
            val request = UpdateDiscordUserLinkRequest(isPrimary = true)
            val expected = createResponse(id = 1L, isPrimary = true)
            every { discordUserLinkService.update(1L, request) } returns expected

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.isPrimary shouldBe true
            verify(exactly = 1) { discordUserLinkService.update(1L, request) }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete link and return 204 status`() {
            // Given
            every { discordUserLinkService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { discordUserLinkService.delete(1L) }
        }
    }

    @Nested
    inner class Exists {
        @Test
        fun `should return true when link exists`() {
            // Given
            every { discordUserLinkService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
        }

        @Test
        fun `should return false when link does not exist`() {
            // Given
            every { discordUserLinkService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByDiscordUserId {
        @Test
        fun `should return all links for Discord user`() {
            // Given
            val expected =
                listOf(
                    createResponse(id = 1L, raiderId = 1L),
                    createResponse(id = 2L, raiderId = 2L),
                )
            every { discordUserLinkService.findByDiscordUserId(discordUserId) } returns expected

            // When
            val result = controller.findByDiscordUserId(discordUserId)

            // Then
            result shouldHaveSize 2
            verify(exactly = 1) { discordUserLinkService.findByDiscordUserId(discordUserId) }
        }
    }

    @Nested
    inner class FindPrimaryByDiscordUserId {
        @Test
        fun `should return primary link`() {
            // Given
            val expected = createResponse(id = 1L, isPrimary = true)
            every { discordUserLinkService.findPrimaryByDiscordUserId(discordUserId) } returns expected

            // When
            val result = controller.findPrimaryByDiscordUserId(discordUserId)

            // Then
            result.isPrimary shouldBe true
        }
    }

    @Nested
    inner class CountByDiscordUserId {
        @Test
        fun `should return count of links`() {
            // Given
            every { discordUserLinkService.countByDiscordUserId(discordUserId) } returns 3L

            // When
            val result = controller.countByDiscordUserId(discordUserId)

            // Then
            result.count shouldBe 3L
        }
    }

    @Nested
    inner class DeleteByDiscordUserId {
        @Test
        fun `should delete all links and return count`() {
            // Given
            every { discordUserLinkService.deleteByDiscordUserId(discordUserId) } returns 2

            // When
            val result = controller.deleteByDiscordUserId(discordUserId)

            // Then
            result.count shouldBe 2L
        }
    }

    @Nested
    inner class FindByRaiderId {
        @Test
        fun `should return all links for raider`() {
            // Given
            val expected =
                listOf(
                    createResponse(id = 1L, discordUserId = "111111111111111111"),
                    createResponse(id = 2L, discordUserId = "222222222222222222"),
                )
            every { discordUserLinkService.findByRaiderId(raiderId) } returns expected

            // When
            val result = controller.findByRaiderId(raiderId)

            // Then
            result shouldHaveSize 2
        }
    }

    @Nested
    inner class SetPrimary {
        @Test
        fun `should set link as primary`() {
            // Given
            val expected = createResponse(id = 1L, isPrimary = true)
            every { discordUserLinkService.setPrimary(1L) } returns expected

            // When
            val result = controller.setPrimary(1L)

            // Then
            result.isPrimary shouldBe true
        }
    }
}
