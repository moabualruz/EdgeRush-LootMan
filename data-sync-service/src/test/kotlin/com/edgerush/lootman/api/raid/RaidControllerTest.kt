package com.edgerush.lootman.api.raid

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.CrudService
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Unit tests for RaidController.
 *
 * Tests controller methods directly without Spring context,
 * using the generic CrudService pattern.
 */
class RaidControllerTest : UnitTest() {

    private lateinit var raidService: RaidCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaidController

    @BeforeEach
    fun setup() {
        raidService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaidController(raidService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createRaidResponse(raidId = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { raidService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                raidService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should use custom page size when provided`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createRaidResponse()),
                page = 0,
                size = 50,
                totalElements = 1,
            )
            every { raidService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = 50)

            // Then
            result shouldBe expectedResponse
            verify {
                raidService.findAll(match { it.page == 0 && it.size == 50 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<RaidResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { raidService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then - maxPageSize is 100, so 500 should be capped to 100
            slot.captured.size shouldBe 100
        }

        @Test
        fun `should return multiple raids`() {
            // Given
            val raids = listOf(
                createRaidResponse(raidId = 1L, instance = "Nerub-ar Palace"),
                createRaidResponse(raidId = 2L, instance = "Vault of the Incarnates"),
                createRaidResponse(raidId = 3L, instance = "Aberrus, the Shadowed Crucible"),
            )
            val expectedResponse = PagedResponse(
                content = raids,
                page = 0,
                size = 20,
                totalElements = 3,
            )
            every { raidService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result.totalElements shouldBe 3
            result.content.size shouldBe 3
            result.content[0].instance shouldBe "Nerub-ar Palace"
            result.content[1].instance shouldBe "Vault of the Incarnates"
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return raid when found`() {
            // Given
            val expectedRaid = createRaidResponse(raidId = 123L, instance = "Nerub-ar Palace")
            every { raidService.findById(123L) } returns expectedRaid

            // When
            val result = controller.findById(123L)

            // Then
            result.raidId shouldBe 123L
            result.instance shouldBe "Nerub-ar Palace"
            verify(exactly = 1) { raidService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { raidService.findById(999L) } throws NoSuchElementException("Raid not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created raid with 201 status`() {
            // Given
            val request = CreateRaidRequest(
                date = LocalDate.of(2024, 3, 15),
                startTime = LocalTime.of(20, 0),
                endTime = LocalTime.of(23, 30),
                instance = "Nerub-ar Palace",
                difficulty = "Mythic",
                optional = false,
                status = "SCHEDULED",
                totalSize = 20,
                teamId = 1L,
            )

            val created = createRaidResponse(
                raidId = 1L,
                date = LocalDate.of(2024, 3, 15),
                instance = "Nerub-ar Palace",
                difficulty = "Mythic",
            )
            every { raidService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.raidId shouldBe 1L
            result.body?.instance shouldBe "Nerub-ar Palace"
            result.body?.difficulty shouldBe "Mythic"
            verify(exactly = 1) { raidService.create(request) }
        }

        @Test
        fun `should pass correct request to service`() {
            // Given
            val requestSlot = slot<CreateRaidRequest>()
            val request = CreateRaidRequest(
                date = LocalDate.of(2024, 6, 20),
                startTime = LocalTime.of(19, 30),
                endTime = null,
                instance = "Vault of the Incarnates",
                difficulty = "Heroic",
                optional = true,
                status = "SCHEDULED",
                totalSize = 25,
                notes = "Alt raid night",
                teamId = 2L,
                seasonId = 5L,
            )

            val created = createRaidResponse(raidId = 42L)
            every { raidService.create(capture(requestSlot)) } returns created

            // When
            controller.create(request)

            // Then
            requestSlot.captured.date shouldBe LocalDate.of(2024, 6, 20)
            requestSlot.captured.instance shouldBe "Vault of the Incarnates"
            requestSlot.captured.difficulty shouldBe "Heroic"
            requestSlot.captured.optional shouldBe true
            requestSlot.captured.notes shouldBe "Alt raid night"
            requestSlot.captured.teamId shouldBe 2L
            requestSlot.captured.seasonId shouldBe 5L
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated raid`() {
            // Given
            val request = UpdateRaidRequest(
                status = "COMPLETED",
                presentSize = 19,
                notes = "Cleared all bosses",
            )

            val updated = createRaidResponse(
                raidId = 1L,
                status = "COMPLETED",
                presentSize = 19,
                notes = "Cleared all bosses",
            )
            every { raidService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.raidId shouldBe 1L
            result.status shouldBe "COMPLETED"
            result.presentSize shouldBe 19
            result.notes shouldBe "Cleared all bosses"
            verify(exactly = 1) { raidService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when raid not found`() {
            // Given
            val request = UpdateRaidRequest(status = "CANCELLED")

            every { raidService.update(999L, request) } throws NoSuchElementException("Raid not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }

        @Test
        fun `should pass correct request to service with partial update`() {
            // Given
            val requestSlot = slot<UpdateRaidRequest>()
            val request = UpdateRaidRequest(
                endTime = LocalTime.of(22, 45),
                presentSize = 18,
            )

            val updated = createRaidResponse(raidId = 5L)
            every { raidService.update(eq(5L), capture(requestSlot)) } returns updated

            // When
            controller.update(5L, request)

            // Then
            requestSlot.captured.endTime shouldBe LocalTime.of(22, 45)
            requestSlot.captured.presentSize shouldBe 18
            requestSlot.captured.status shouldBe null
            requestSlot.captured.notes shouldBe null
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { raidService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { raidService.delete(1L) }
        }

        @Test
        fun `should propagate exception when raid not found`() {
            // Given
            every { raidService.delete(999L) } throws NoSuchElementException("Raid not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when raid exists`() {
            // Given
            every { raidService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { raidService.existsById(1L) }
        }

        @Test
        fun `should return exists false when raid does not exist`() {
            // Given
            every { raidService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByTeamTests {

        @Test
        fun `should return raids for a team with pagination`() {
            // Given
            val teamId = 1L
            val raids = listOf(
                createRaidResponse(raidId = 1L, teamId = teamId),
                createRaidResponse(raidId = 2L, teamId = teamId),
            )
            val expectedResponse = PagedResponse(
                content = raids,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { raidService.findByTeam(teamId, any()) } returns expectedResponse

            // When
            val result = controller.findByTeam(teamId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.teamId == teamId } shouldBe true
            verify {
                raidService.findByTeam(teamId, match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should return empty response when team has no raids`() {
            // Given
            val expectedResponse = PagedResponse<RaidResponse>(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
            )
            every { raidService.findByTeam(999L, any()) } returns expectedResponse

            // When
            val result = controller.findByTeam(999L, page = 0, size = null)

            // Then
            result.totalElements shouldBe 0
            result.content shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByDateRangeTests {

        @Test
        fun `should return raids within date range`() {
            // Given
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 3, 31)
            val raids = listOf(
                createRaidResponse(raidId = 1L, date = LocalDate.of(2024, 1, 15)),
                createRaidResponse(raidId = 2L, date = LocalDate.of(2024, 2, 20)),
            )
            val expectedResponse = PagedResponse(
                content = raids,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { raidService.findByDateRange(startDate, endDate, any()) } returns expectedResponse

            // When
            val result = controller.findByDateRange(startDate, endDate, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.size shouldBe 2
            verify {
                raidService.findByDateRange(startDate, endDate, match { it.page == 0 })
            }
        }

        @Test
        fun `should return empty response when no raids in date range`() {
            // Given
            val startDate = LocalDate.of(2020, 1, 1)
            val endDate = LocalDate.of(2020, 12, 31)
            val expectedResponse = PagedResponse<RaidResponse>(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
            )
            every { raidService.findByDateRange(startDate, endDate, any()) } returns expectedResponse

            // When
            val result = controller.findByDateRange(startDate, endDate, page = 0, size = null)

            // Then
            result.totalElements shouldBe 0
            result.content shouldBe emptyList()
        }
    }

    @Nested
    inner class CountByTeamTests {

        @Test
        fun `should return count for team`() {
            // Given
            val teamId = 1L
            every { raidService.countByTeam(teamId) } returns 42L

            // When
            val result = controller.countByTeam(teamId)

            // Then
            result.count shouldBe 42L
            verify(exactly = 1) { raidService.countByTeam(teamId) }
        }

        @Test
        fun `should return zero count for team with no raids`() {
            // Given
            every { raidService.countByTeam(999L) } returns 0L

            // When
            val result = controller.countByTeam(999L)

            // Then
            result.count shouldBe 0L
        }
    }

    private fun createRaidResponse(
        raidId: Long = 1L,
        date: LocalDate? = LocalDate.of(2024, 3, 15),
        startTime: LocalTime? = LocalTime.of(20, 0),
        endTime: LocalTime? = LocalTime.of(23, 30),
        instance: String? = "Nerub-ar Palace",
        difficulty: String? = "Mythic",
        optional: Boolean? = false,
        status: String? = "SCHEDULED",
        presentSize: Int? = null,
        totalSize: Int? = 20,
        notes: String? = null,
        selectionsImage: String? = null,
        teamId: Long? = 1L,
        seasonId: Long? = 1L,
        periodId: Long? = null,
        createdAt: OffsetDateTime? = OffsetDateTime.now(),
        updatedAt: OffsetDateTime? = OffsetDateTime.now(),
        syncedAt: OffsetDateTime = OffsetDateTime.now(),
    ): RaidResponse = RaidResponse(
        raidId = raidId,
        date = date,
        startTime = startTime,
        endTime = endTime,
        instance = instance,
        difficulty = difficulty,
        optional = optional,
        status = status,
        presentSize = presentSize,
        totalSize = totalSize,
        notes = notes,
        selectionsImage = selectionsImage,
        teamId = teamId,
        seasonId = seasonId,
        periodId = periodId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
    )
}
