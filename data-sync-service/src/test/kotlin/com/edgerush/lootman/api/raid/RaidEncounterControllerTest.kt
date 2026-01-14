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
 * Unit tests for RaidEncounterController.
 *
 * Tests controller methods directly without Spring context.
 */
class RaidEncounterControllerTest : UnitTest() {

    private lateinit var encounterService: RaidEncounterCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaidEncounterController

    @BeforeEach
    fun setup() {
        encounterService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaidEncounterController(encounterService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createEncounterResponse(id = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { encounterService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                encounterService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should use custom page size when provided`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createEncounterResponse()),
                page = 0,
                size = 50,
                totalElements = 1,
            )
            every { encounterService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = 50)

            // Then
            result shouldBe expectedResponse
            verify {
                encounterService.findAll(match { it.page == 0 && it.size == 50 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<RaidEncounterResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { encounterService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return encounter when found`() {
            // Given
            val expectedEncounter = createEncounterResponse(id = 123L, name = "Queen Ansurek")
            every { encounterService.findById(123L) } returns expectedEncounter

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.name shouldBe "Queen Ansurek"
            verify(exactly = 1) { encounterService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { encounterService.findById(999L) } throws NoSuchElementException("Encounter not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created encounter with 201 status`() {
            // Given
            val request = CreateRaidEncounterRequest(
                raidId = 1L,
                encounterId = 2902L,
                name = "Queen Ansurek",
                enabled = true,
                extra = false,
                notes = "Final boss",
            )

            val created = createEncounterResponse(
                id = 1L,
                raidId = 1L,
                encounterId = 2902L,
                name = "Queen Ansurek",
            )
            every { encounterService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.name shouldBe "Queen Ansurek"
            verify(exactly = 1) { encounterService.create(request) }
        }

        @Test
        fun `should pass correct request to service`() {
            // Given
            val requestSlot = slot<CreateRaidEncounterRequest>()
            val request = CreateRaidEncounterRequest(
                raidId = 5L,
                encounterId = 2901L,
                name = "Nexus-Princess Ky'veza",
                enabled = true,
                extra = false,
            )

            val created = createEncounterResponse(id = 42L)
            every { encounterService.create(capture(requestSlot)) } returns created

            // When
            controller.create(request)

            // Then
            requestSlot.captured.raidId shouldBe 5L
            requestSlot.captured.encounterId shouldBe 2901L
            requestSlot.captured.name shouldBe "Nexus-Princess Ky'veza"
            requestSlot.captured.enabled shouldBe true
            requestSlot.captured.extra shouldBe false
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated encounter`() {
            // Given
            val request = UpdateRaidEncounterRequest(
                enabled = false,
                notes = "Skipping this week",
            )

            val updated = createEncounterResponse(
                id = 1L,
                enabled = false,
                notes = "Skipping this week",
            )
            every { encounterService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.enabled shouldBe false
            result.notes shouldBe "Skipping this week"
            verify(exactly = 1) { encounterService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when encounter not found`() {
            // Given
            val request = UpdateRaidEncounterRequest(enabled = false)

            every { encounterService.update(999L, request) } throws NoSuchElementException("Encounter not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { encounterService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { encounterService.delete(1L) }
        }

        @Test
        fun `should propagate exception when encounter not found`() {
            // Given
            every { encounterService.delete(999L) } throws NoSuchElementException("Encounter not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when encounter exists`() {
            // Given
            every { encounterService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { encounterService.existsById(1L) }
        }

        @Test
        fun `should return exists false when encounter does not exist`() {
            // Given
            every { encounterService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByRaidTests {

        @Test
        fun `should return encounters for a raid with pagination`() {
            // Given
            val raidId = 1L
            val encounters = listOf(
                createEncounterResponse(id = 1L, raidId = raidId, name = "Ulgrax"),
                createEncounterResponse(id = 2L, raidId = raidId, name = "Bloodbound Horror"),
            )
            val expectedResponse = PagedResponse(
                content = encounters,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { encounterService.findByRaid(raidId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaid(raidId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.raidId == raidId } shouldBe true
            verify {
                encounterService.findByRaid(raidId, match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should return empty response when raid has no encounters`() {
            // Given
            val expectedResponse = PagedResponse<RaidEncounterResponse>(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
            )
            every { encounterService.findByRaid(999L, any()) } returns expectedResponse

            // When
            val result = controller.findByRaid(999L, page = 0, size = null)

            // Then
            result.totalElements shouldBe 0
            result.content shouldBe emptyList()
        }
    }

    @Nested
    inner class CountByRaidTests {

        @Test
        fun `should return count for raid`() {
            // Given
            val raidId = 1L
            every { encounterService.countByRaid(raidId) } returns 8L

            // When
            val result = controller.countByRaid(raidId)

            // Then
            result.count shouldBe 8L
            verify(exactly = 1) { encounterService.countByRaid(raidId) }
        }

        @Test
        fun `should return zero count for raid with no encounters`() {
            // Given
            every { encounterService.countByRaid(999L) } returns 0L

            // When
            val result = controller.countByRaid(999L)

            // Then
            result.count shouldBe 0L
        }
    }

    @Nested
    inner class FindEnabledByRaidTests {

        @Test
        fun `should return only enabled encounters for a raid`() {
            // Given
            val raidId = 1L
            val encounters = listOf(
                createEncounterResponse(id = 1L, raidId = raidId, enabled = true),
                createEncounterResponse(id = 2L, raidId = raidId, enabled = true),
            )
            val expectedResponse = PagedResponse(
                content = encounters,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { encounterService.findEnabledByRaid(raidId, any()) } returns expectedResponse

            // When
            val result = controller.findEnabledByRaid(raidId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.enabled == true } shouldBe true
        }
    }

    private fun createEncounterResponse(
        id: Long = 1L,
        raidId: Long = 1L,
        encounterId: Long? = 2900L,
        name: String? = "Test Encounter",
        enabled: Boolean? = true,
        extra: Boolean? = false,
        notes: String? = null,
    ): RaidEncounterResponse = RaidEncounterResponse(
        id = id,
        raidId = raidId,
        encounterId = encounterId,
        name = name,
        enabled = enabled,
        extra = extra,
        notes = notes,
    )
}
