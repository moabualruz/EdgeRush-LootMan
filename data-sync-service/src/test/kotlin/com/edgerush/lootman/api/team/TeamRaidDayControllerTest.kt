package com.edgerush.lootman.api.team

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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Unit tests for TeamRaidDayController.
 */
class TeamRaidDayControllerTest : UnitTest() {
    private lateinit var teamRaidDayService: TeamRaidDayCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: TeamRaidDayController

    @BeforeEach
    fun setup() {
        teamRaidDayService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = TeamRaidDayController(teamRaidDayService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createTeamRaidDayResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { teamRaidDayService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                teamRaidDayService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<TeamRaidDayResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { teamRaidDayService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return team raid day when found`() {
            // Given
            val expected = createTeamRaidDayResponse(id = 123L, weekDay = "Wednesday")
            every { teamRaidDayService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.weekDay shouldBe "Wednesday"
            verify(exactly = 1) { teamRaidDayService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { teamRaidDayService.findById(999L) } throws NoSuchElementException("Team raid day not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team raid day not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created team raid day with 201 status`() {
            // Given
            val request =
                CreateTeamRaidDayRequest(
                    teamId = 1L,
                    weekDay = "Wednesday",
                    startTime = LocalTime.of(20, 0),
                    endTime = LocalTime.of(23, 0),
                    currentInstance = "Nerub-ar Palace",
                    difficulty = "Mythic",
                    activeFrom = LocalDate.of(2024, 1, 1),
                )

            val created =
                createTeamRaidDayResponse(
                    id = 1L,
                    teamId = 1L,
                    weekDay = "Wednesday",
                )
            every { teamRaidDayService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.weekDay shouldBe "Wednesday"
            verify(exactly = 1) { teamRaidDayService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated team raid day`() {
            // Given
            val request =
                UpdateTeamRaidDayRequest(
                    startTime = LocalTime.of(19, 30),
                    endTime = LocalTime.of(22, 30),
                )

            val updated =
                createTeamRaidDayResponse(
                    id = 1L,
                    startTime = LocalTime.of(19, 30),
                    endTime = LocalTime.of(22, 30),
                )
            every { teamRaidDayService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.startTime shouldBe LocalTime.of(19, 30)
            result.endTime shouldBe LocalTime.of(22, 30)
            verify(exactly = 1) { teamRaidDayService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when team raid day not found`() {
            // Given
            val request = UpdateTeamRaidDayRequest(weekDay = "Thursday")

            every { teamRaidDayService.update(999L, request) } throws NoSuchElementException("Team raid day not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team raid day not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { teamRaidDayService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { teamRaidDayService.delete(1L) }
        }

        @Test
        fun `should propagate exception when team raid day not found`() {
            // Given
            every { teamRaidDayService.delete(999L) } throws NoSuchElementException("Team raid day not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team raid day not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when team raid day exists`() {
            // Given
            every { teamRaidDayService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { teamRaidDayService.existsById(1L) }
        }

        @Test
        fun `should return exists false when team raid day does not exist`() {
            // Given
            every { teamRaidDayService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByTeamIdTests {
        @Test
        fun `should return team raid days for a team`() {
            // Given
            val teamId = 1L
            val raidDays =
                listOf(
                    createTeamRaidDayResponse(id = 1L, teamId = teamId, weekDay = "Wednesday"),
                    createTeamRaidDayResponse(id = 2L, teamId = teamId, weekDay = "Sunday"),
                )
            val expectedResponse =
                PagedResponse(
                    content = raidDays,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { teamRaidDayService.findByTeamId(teamId, any()) } returns expectedResponse

            // When
            val result = controller.findByTeamId(teamId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.teamId == teamId } shouldBe true
        }
    }

    @Nested
    inner class CountByTeamIdTests {
        @Test
        fun `should return count for team`() {
            // Given
            val teamId = 1L
            every { teamRaidDayService.countByTeamId(teamId) } returns 2L

            // When
            val result = controller.countByTeamId(teamId)

            // Then
            result.count shouldBe 2L
            verify(exactly = 1) { teamRaidDayService.countByTeamId(teamId) }
        }
    }

    private fun createTeamRaidDayResponse(
        id: Long = 1L,
        teamId: Long = 1L,
        weekDay: String? = "Wednesday",
        startTime: LocalTime? = LocalTime.of(20, 0),
        endTime: LocalTime? = LocalTime.of(23, 0),
        currentInstance: String? = "Nerub-ar Palace",
        difficulty: String? = "Mythic",
        activeFrom: LocalDate? = LocalDate.of(2024, 1, 1),
        syncedAt: OffsetDateTime = OffsetDateTime.now(),
    ): TeamRaidDayResponse =
        TeamRaidDayResponse(
            id = id,
            teamId = teamId,
            weekDay = weekDay,
            startTime = startTime,
            endTime = endTime,
            currentInstance = currentInstance,
            difficulty = difficulty,
            activeFrom = activeFrom,
            syncedAt = syncedAt,
        )
}
