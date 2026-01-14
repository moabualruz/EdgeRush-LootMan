package com.edgerush.lootman.api.behavioral

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
 * Unit tests for BehavioralActionController.
 */
class BehavioralActionControllerTest : UnitTest() {

    private lateinit var behavioralActionService: BehavioralActionCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: BehavioralActionController

    @BeforeEach
    fun setup() {
        behavioralActionService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = BehavioralActionController(behavioralActionService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createBehavioralActionResponse(id = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { behavioralActionService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                behavioralActionService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<BehavioralActionResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { behavioralActionService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return behavioral action when found`() {
            // Given
            val expected = createBehavioralActionResponse(id = 123L, characterName = "Testchar")
            every { behavioralActionService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
            verify(exactly = 1) { behavioralActionService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { behavioralActionService.findById(999L) } throws NoSuchElementException("Behavioral action not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Behavioral action not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created behavioral action with 201 status`() {
            // Given
            val request = CreateBehavioralActionRequest(
                guildId = "my-guild",
                characterName = "Badplayer",
                actionType = "DEDUCTION",
                deductionAmount = 0.5,
                reason = "Ninja looting",
                appliedBy = "GuildLeader",
                expiresAt = LocalDateTime.now().plusDays(30),
            )

            val created = createBehavioralActionResponse(
                id = 1L,
                characterName = "Badplayer",
                actionType = "DEDUCTION",
                deductionAmount = 0.5,
            )
            every { behavioralActionService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.characterName shouldBe "Badplayer"
            result.body?.actionType shouldBe "DEDUCTION"
            verify(exactly = 1) { behavioralActionService.create(request) }
        }

        @Test
        fun `should create permanent action when expiresAt is null`() {
            // Given
            val request = CreateBehavioralActionRequest(
                guildId = "my-guild",
                characterName = "Cheater",
                actionType = "DEDUCTION",
                deductionAmount = 1.0,
                reason = "Exploiting",
                appliedBy = "Admin",
                expiresAt = null,
            )

            val created = createBehavioralActionResponse(
                id = 1L,
                characterName = "Cheater",
                expiresAt = null,
            )
            every { behavioralActionService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.body?.expiresAt shouldBe null
        }

        @Test
        fun `should create restoration action`() {
            // Given
            val request = CreateBehavioralActionRequest(
                guildId = "my-guild",
                characterName = "Goodplayer",
                actionType = "RESTORATION",
                deductionAmount = 0.0,
                reason = "Good behavior",
                appliedBy = "GuildLeader",
                expiresAt = null,
            )

            val created = createBehavioralActionResponse(
                id = 1L,
                characterName = "Goodplayer",
                actionType = "RESTORATION",
                deductionAmount = 0.0,
            )
            every { behavioralActionService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.body?.actionType shouldBe "RESTORATION"
            result.body?.deductionAmount shouldBe 0.0
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated behavioral action`() {
            // Given
            val request = UpdateBehavioralActionRequest(
                reason = "Updated reason",
                isActive = false,
            )

            val updated = createBehavioralActionResponse(
                id = 1L,
                reason = "Updated reason",
                isActive = false,
            )
            every { behavioralActionService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.reason shouldBe "Updated reason"
            result.isActive shouldBe false
            verify(exactly = 1) { behavioralActionService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when behavioral action not found`() {
            // Given
            val request = UpdateBehavioralActionRequest(isActive = false)

            every { behavioralActionService.update(999L, request) } throws NoSuchElementException("Behavioral action not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Behavioral action not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { behavioralActionService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { behavioralActionService.delete(1L) }
        }

        @Test
        fun `should propagate exception when behavioral action not found`() {
            // Given
            every { behavioralActionService.delete(999L) } throws NoSuchElementException("Behavioral action not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Behavioral action not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when behavioral action exists`() {
            // Given
            every { behavioralActionService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { behavioralActionService.existsById(1L) }
        }

        @Test
        fun `should return exists false when behavioral action does not exist`() {
            // Given
            every { behavioralActionService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByGuildTests {

        @Test
        fun `should return behavioral actions for a guild`() {
            // Given
            val guildId = "my-guild"
            val actions = listOf(
                createBehavioralActionResponse(id = 1L, guildId = guildId),
                createBehavioralActionResponse(id = 2L, guildId = guildId),
            )
            val expectedResponse = PagedResponse(
                content = actions,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { behavioralActionService.findByGuild(guildId, any()) } returns expectedResponse

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
        fun `should return only active behavioral actions for a guild`() {
            // Given
            val guildId = "my-guild"
            val actions = listOf(
                createBehavioralActionResponse(id = 1L, guildId = guildId, isActive = true),
                createBehavioralActionResponse(id = 2L, guildId = guildId, isActive = true),
            )
            val expectedResponse = PagedResponse(
                content = actions,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { behavioralActionService.findActiveByGuild(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findActiveByGuild(guildId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.isActive } shouldBe true
        }
    }

    @Nested
    inner class FindByCharacterTests {

        @Test
        fun `should return behavioral actions for a character`() {
            // Given
            val guildId = "my-guild"
            val characterName = "Testchar"
            val actions = listOf(
                createBehavioralActionResponse(id = 1L, guildId = guildId, characterName = characterName),
            )
            val expectedResponse = PagedResponse(
                content = actions,
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { behavioralActionService.findByCharacter(guildId, characterName, any()) } returns expectedResponse

            // When
            val result = controller.findByCharacter(guildId, characterName, page = 0, size = null)

            // Then
            result.totalElements shouldBe 1
            result.content.all { it.characterName == characterName } shouldBe true
        }
    }

    @Nested
    inner class GetTotalDeductionTests {

        @Test
        fun `should return total deduction for character`() {
            // Given
            val guildId = "my-guild"
            val characterName = "Badplayer"
            every { behavioralActionService.getTotalDeduction(guildId, characterName) } returns 0.75

            // When
            val result = controller.getTotalDeduction(guildId, characterName)

            // Then
            result.totalDeduction shouldBe 0.75
            verify(exactly = 1) { behavioralActionService.getTotalDeduction(guildId, characterName) }
        }

        @Test
        fun `should return zero deduction for character with no actions`() {
            // Given
            val guildId = "my-guild"
            val characterName = "Goodplayer"
            every { behavioralActionService.getTotalDeduction(guildId, characterName) } returns 0.0

            // When
            val result = controller.getTotalDeduction(guildId, characterName)

            // Then
            result.totalDeduction shouldBe 0.0
        }
    }

    @Nested
    inner class CountByGuildTests {

        @Test
        fun `should return count for guild`() {
            // Given
            val guildId = "my-guild"
            every { behavioralActionService.countByGuild(guildId) } returns 5L

            // When
            val result = controller.countByGuild(guildId)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { behavioralActionService.countByGuild(guildId) }
        }
    }

    private fun createBehavioralActionResponse(
        id: Long = 1L,
        guildId: String = "test-guild",
        characterName: String = "Testchar",
        actionType: String = "DEDUCTION",
        deductionAmount: Double = 0.5,
        reason: String = "Test reason",
        appliedBy: String = "Admin",
        appliedAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = LocalDateTime.now().plusDays(30),
        isActive: Boolean = true,
    ): BehavioralActionResponse = BehavioralActionResponse(
        id = id,
        guildId = guildId,
        characterName = characterName,
        actionType = actionType,
        deductionAmount = deductionAmount,
        reason = reason,
        appliedBy = appliedBy,
        appliedAt = appliedAt,
        expiresAt = expiresAt,
        isActive = isActive,
    )
}
