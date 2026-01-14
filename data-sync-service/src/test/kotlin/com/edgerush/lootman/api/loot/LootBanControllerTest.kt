package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

/**
 * Unit tests for LootBanController.
 */
class LootBanControllerTest : UnitTest() {

    private lateinit var lootBanService: LootBanCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: LootBanController

    @BeforeEach
    fun setup() {
        lootBanService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = LootBanController(lootBanService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createLootBanResponse(id = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { lootBanService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                lootBanService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<LootBanResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { lootBanService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return loot ban when found`() {
            // Given
            val expectedBan = createLootBanResponse(id = 123L, characterName = "Testchar")
            every { lootBanService.findById(123L) } returns expectedBan

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
            verify(exactly = 1) { lootBanService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { lootBanService.findById(999L) } throws NoSuchElementException("Loot ban not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Loot ban not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created loot ban with 201 status`() {
            // Given
            val request = CreateLootBanEntityRequest(
                guildId = "my-guild",
                characterName = "Badplayer",
                reason = "Ninja looting",
                bannedBy = "GuildLeader",
                expiresAt = LocalDateTime.now().plusDays(30),
            )

            val created = createLootBanResponse(
                id = 1L,
                characterName = "Badplayer",
                reason = "Ninja looting",
            )
            every { lootBanService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.characterName shouldBe "Badplayer"
            result.body?.reason shouldBe "Ninja looting"
            verify(exactly = 1) { lootBanService.create(request) }
        }

        @Test
        fun `should create permanent ban when expiresAt is null`() {
            // Given
            val request = CreateLootBanEntityRequest(
                guildId = "my-guild",
                characterName = "Cheater",
                reason = "Exploiting",
                bannedBy = "Admin",
                expiresAt = null,
            )

            val created = createLootBanResponse(
                id = 1L,
                characterName = "Cheater",
                expiresAt = null,
            )
            every { lootBanService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.body?.expiresAt shouldBe null
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated loot ban`() {
            // Given
            val request = UpdateLootBanEntityRequest(
                reason = "Updated reason",
                isActive = false,
            )

            val updated = createLootBanResponse(
                id = 1L,
                reason = "Updated reason",
                isActive = false,
            )
            every { lootBanService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.reason shouldBe "Updated reason"
            result.isActive shouldBe false
            verify(exactly = 1) { lootBanService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when loot ban not found`() {
            // Given
            val request = UpdateLootBanEntityRequest(isActive = false)

            every { lootBanService.update(999L, request) } throws NoSuchElementException("Loot ban not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Loot ban not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { lootBanService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { lootBanService.delete(1L) }
        }

        @Test
        fun `should propagate exception when loot ban not found`() {
            // Given
            every { lootBanService.delete(999L) } throws NoSuchElementException("Loot ban not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Loot ban not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when loot ban exists`() {
            // Given
            every { lootBanService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { lootBanService.existsById(1L) }
        }

        @Test
        fun `should return exists false when loot ban does not exist`() {
            // Given
            every { lootBanService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByGuildTests {

        @Test
        fun `should return loot bans for a guild`() {
            // Given
            val guildId = "my-guild"
            val bans = listOf(
                createLootBanResponse(id = 1L, guildId = guildId),
                createLootBanResponse(id = 2L, guildId = guildId),
            )
            val expectedResponse = PagedResponse(
                content = bans,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { lootBanService.findByGuild(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findByGuild(guildId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.guildId == guildId } shouldBe true
        }
    }

    @Nested
    inner class FindActiveByGuildTests {

        @Test
        fun `should return only active loot bans for a guild`() {
            // Given
            val guildId = "my-guild"
            val bans = listOf(
                createLootBanResponse(id = 1L, guildId = guildId, isActive = true),
                createLootBanResponse(id = 2L, guildId = guildId, isActive = true),
            )
            val expectedResponse = PagedResponse(
                content = bans,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { lootBanService.findActiveByGuild(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findActiveByGuild(guildId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.isActive } shouldBe true
        }
    }

    @Nested
    inner class CheckBanTests {

        @Test
        fun `should return true when character is banned`() {
            // Given
            val guildId = "my-guild"
            val characterName = "Badplayer"
            every { lootBanService.isCharacterBanned(guildId, characterName) } returns true

            // When
            val result = controller.checkBan(guildId, characterName)

            // Then
            result.banned shouldBe true
        }

        @Test
        fun `should return false when character is not banned`() {
            // Given
            val guildId = "my-guild"
            val characterName = "Goodplayer"
            every { lootBanService.isCharacterBanned(guildId, characterName) } returns false

            // When
            val result = controller.checkBan(guildId, characterName)

            // Then
            result.banned shouldBe false
        }
    }

    @Nested
    inner class CountByGuildTests {

        @Test
        fun `should return count for guild`() {
            // Given
            val guildId = "my-guild"
            every { lootBanService.countByGuild(guildId) } returns 5L

            // When
            val result = controller.countByGuild(guildId)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { lootBanService.countByGuild(guildId) }
        }
    }

    private fun createLootBanResponse(
        id: Long = 1L,
        guildId: String = "test-guild",
        characterName: String = "Testchar",
        reason: String = "Test reason",
        bannedBy: String = "Admin",
        bannedAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = LocalDateTime.now().plusDays(30),
        isActive: Boolean = true,
    ): LootBanResponse = LootBanResponse(
        id = id,
        guildId = guildId,
        characterName = characterName,
        reason = reason,
        bannedBy = bannedBy,
        bannedAt = bannedAt,
        expiresAt = expiresAt,
        isActive = isActive,
    )
}
