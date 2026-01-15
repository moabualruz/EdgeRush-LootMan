package com.edgerush.lootman.api.raid

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

/**
 * Unit tests for RaidSignupController.
 *
 * Tests controller methods directly without Spring context.
 */
class RaidSignupControllerTest : UnitTest() {
    private lateinit var signupService: RaidSignupCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaidSignupController

    @BeforeEach
    fun setup() {
        signupService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaidSignupController(signupService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createSignupResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { signupService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                signupService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<RaidSignupResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { signupService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return signup when found`() {
            // Given
            val expectedSignup = createSignupResponse(id = 123L, characterName = "Testchar")
            every { signupService.findById(123L) } returns expectedSignup

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
            verify(exactly = 1) { signupService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { signupService.findById(999L) } throws NoSuchElementException("Signup not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Signup not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created signup with 201 status`() {
            // Given
            val request =
                CreateRaidSignupRequest(
                    raidId = 1L,
                    characterId = 42L,
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "WARRIOR",
                    characterRole = "DPS",
                    status = "ACCEPTED",
                    selected = true,
                )

            val created =
                createSignupResponse(
                    id = 1L,
                    raidId = 1L,
                    characterName = "Testchar",
                )
            every { signupService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.characterName shouldBe "Testchar"
            verify(exactly = 1) { signupService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated signup`() {
            // Given
            val request =
                UpdateRaidSignupRequest(
                    status = "DECLINED",
                    comment = "Cannot attend",
                )

            val updated =
                createSignupResponse(
                    id = 1L,
                    status = "DECLINED",
                    comment = "Cannot attend",
                )
            every { signupService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.status shouldBe "DECLINED"
            result.comment shouldBe "Cannot attend"
            verify(exactly = 1) { signupService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when signup not found`() {
            // Given
            val request = UpdateRaidSignupRequest(status = "DECLINED")

            every { signupService.update(999L, request) } throws NoSuchElementException("Signup not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Signup not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { signupService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { signupService.delete(1L) }
        }

        @Test
        fun `should propagate exception when signup not found`() {
            // Given
            every { signupService.delete(999L) } throws NoSuchElementException("Signup not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Signup not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when signup exists`() {
            // Given
            every { signupService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { signupService.existsById(1L) }
        }

        @Test
        fun `should return exists false when signup does not exist`() {
            // Given
            every { signupService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByRaidTests {
        @Test
        fun `should return signups for a raid with pagination`() {
            // Given
            val raidId = 1L
            val signups =
                listOf(
                    createSignupResponse(id = 1L, raidId = raidId, characterName = "Player1"),
                    createSignupResponse(id = 2L, raidId = raidId, characterName = "Player2"),
                )
            val expectedResponse =
                PagedResponse(
                    content = signups,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { signupService.findByRaid(raidId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaid(raidId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.raidId == raidId } shouldBe true
            verify {
                signupService.findByRaid(raidId, match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should return empty response when raid has no signups`() {
            // Given
            val expectedResponse =
                PagedResponse<RaidSignupResponse>(
                    content = emptyList(),
                    page = 0,
                    size = 20,
                    totalElements = 0,
                )
            every { signupService.findByRaid(999L, any()) } returns expectedResponse

            // When
            val result = controller.findByRaid(999L, page = 0, size = null)

            // Then
            result.totalElements shouldBe 0
            result.content shouldBe emptyList()
        }
    }

    @Nested
    inner class FindSelectedByRaidTests {
        @Test
        fun `should return only selected signups for a raid`() {
            // Given
            val raidId = 1L
            val signups =
                listOf(
                    createSignupResponse(id = 1L, raidId = raidId, selected = true),
                    createSignupResponse(id = 2L, raidId = raidId, selected = true),
                )
            val expectedResponse =
                PagedResponse(
                    content = signups,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { signupService.findSelectedByRaid(raidId, any()) } returns expectedResponse

            // When
            val result = controller.findSelectedByRaid(raidId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.selected == true } shouldBe true
        }
    }

    @Nested
    inner class CountByRaidTests {
        @Test
        fun `should return count for raid`() {
            // Given
            val raidId = 1L
            every { signupService.countByRaid(raidId) } returns 25L

            // When
            val result = controller.countByRaid(raidId)

            // Then
            result.count shouldBe 25L
            verify(exactly = 1) { signupService.countByRaid(raidId) }
        }

        @Test
        fun `should return zero count for raid with no signups`() {
            // Given
            every { signupService.countByRaid(999L) } returns 0L

            // When
            val result = controller.countByRaid(999L)

            // Then
            result.count shouldBe 0L
        }
    }

    @Nested
    inner class FindByCharacterTests {
        @Test
        fun `should return signups for a character`() {
            // Given
            val characterId = 42L
            val signups =
                listOf(
                    createSignupResponse(id = 1L, characterId = characterId, raidId = 1L),
                    createSignupResponse(id = 2L, characterId = characterId, raidId = 2L),
                )
            val expectedResponse =
                PagedResponse(
                    content = signups,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { signupService.findByCharacter(characterId, any()) } returns expectedResponse

            // When
            val result = controller.findByCharacter(characterId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.characterId == characterId } shouldBe true
        }
    }

    private fun createSignupResponse(
        id: Long = 1L,
        raidId: Long = 1L,
        characterId: Long? = 42L,
        characterName: String? = "Testchar",
        characterRealm: String? = "TestRealm",
        characterRegion: String? = "EU",
        characterClass: String? = "WARRIOR",
        characterRole: String? = "DPS",
        characterGuest: Boolean? = false,
        status: String? = "ACCEPTED",
        comment: String? = null,
        selected: Boolean? = true,
    ): RaidSignupResponse =
        RaidSignupResponse(
            id = id,
            raidId = raidId,
            characterId = characterId,
            characterName = characterName,
            characterRealm = characterRealm,
            characterRegion = characterRegion,
            characterClass = characterClass,
            characterRole = characterRole,
            characterGuest = characterGuest,
            status = status,
            comment = comment,
            selected = selected,
        )
}
