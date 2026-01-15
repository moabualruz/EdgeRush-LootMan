package com.edgerush.lootman.api.attendance

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
import java.time.OffsetDateTime

/**
 * Unit tests for AttendanceStatController.
 */
class AttendanceStatControllerTest : UnitTest() {
    private lateinit var attendanceStatService: AttendanceStatCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: AttendanceStatController

    @BeforeEach
    fun setup() {
        attendanceStatService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = AttendanceStatController(attendanceStatService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createAttendanceStatResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { attendanceStatService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                attendanceStatService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<AttendanceStatResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { attendanceStatService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return attendance stat when found`() {
            // Given
            val expected = createAttendanceStatResponse(id = 123L, characterName = "Testchar")
            every { attendanceStatService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
            verify(exactly = 1) { attendanceStatService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { attendanceStatService.findById(999L) } throws NoSuchElementException("Attendance stat not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Attendance stat not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created attendance stat with 201 status`() {
            // Given
            val request =
                CreateAttendanceStatRequest(
                    instance = "Nerub-ar Palace",
                    encounter = "Ulgrax the Devourer",
                    startDate = LocalDate.of(2024, 1, 1),
                    endDate = LocalDate.of(2024, 1, 31),
                    characterId = 100L,
                    characterName = "Testchar",
                    characterRealm = "Silvermoon",
                    characterClass = "Warrior",
                    characterRole = "Tank",
                    characterRegion = "EU",
                    attendedAmountOfRaids = 4,
                    totalAmountOfRaids = 5,
                    attendedPercentage = 0.80,
                    selectedAmountOfEncounters = 8,
                    totalAmountOfEncounters = 10,
                    selectedPercentage = 0.80,
                    teamId = 1L,
                    seasonId = 1L,
                    periodId = 1L,
                )

            val created =
                createAttendanceStatResponse(
                    id = 1L,
                    characterName = "Testchar",
                    attendedPercentage = 0.80,
                )
            every { attendanceStatService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.characterName shouldBe "Testchar"
            result.body?.attendedPercentage shouldBe 0.80
            verify(exactly = 1) { attendanceStatService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated attendance stat`() {
            // Given
            val request =
                UpdateAttendanceStatRequest(
                    attendedAmountOfRaids = 5,
                    attendedPercentage = 1.0,
                )

            val updated =
                createAttendanceStatResponse(
                    id = 1L,
                    attendedAmountOfRaids = 5,
                    attendedPercentage = 1.0,
                )
            every { attendanceStatService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.attendedAmountOfRaids shouldBe 5
            result.attendedPercentage shouldBe 1.0
            verify(exactly = 1) { attendanceStatService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when attendance stat not found`() {
            // Given
            val request = UpdateAttendanceStatRequest(attendedAmountOfRaids = 5)

            every { attendanceStatService.update(999L, request) } throws NoSuchElementException("Attendance stat not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Attendance stat not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { attendanceStatService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { attendanceStatService.delete(1L) }
        }

        @Test
        fun `should propagate exception when attendance stat not found`() {
            // Given
            every { attendanceStatService.delete(999L) } throws NoSuchElementException("Attendance stat not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Attendance stat not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when attendance stat exists`() {
            // Given
            every { attendanceStatService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { attendanceStatService.existsById(1L) }
        }

        @Test
        fun `should return exists false when attendance stat does not exist`() {
            // Given
            every { attendanceStatService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByCharacterIdTests {
        @Test
        fun `should return attendance stats for a character`() {
            // Given
            val characterId = 100L
            val stats =
                listOf(
                    createAttendanceStatResponse(id = 1L, characterId = characterId),
                    createAttendanceStatResponse(id = 2L, characterId = characterId),
                )
            val expectedResponse =
                PagedResponse(
                    content = stats,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { attendanceStatService.findByCharacterId(characterId, any()) } returns expectedResponse

            // When
            val result = controller.findByCharacterId(characterId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.characterId == characterId } shouldBe true
        }
    }

    @Nested
    inner class FindByTeamIdTests {
        @Test
        fun `should return attendance stats for a team`() {
            // Given
            val teamId = 1L
            val stats =
                listOf(
                    createAttendanceStatResponse(id = 1L, teamId = teamId),
                    createAttendanceStatResponse(id = 2L, teamId = teamId),
                )
            val expectedResponse =
                PagedResponse(
                    content = stats,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { attendanceStatService.findByTeamId(teamId, any()) } returns expectedResponse

            // When
            val result = controller.findByTeamId(teamId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.teamId == teamId } shouldBe true
        }
    }

    @Nested
    inner class FindBySeasonIdTests {
        @Test
        fun `should return attendance stats for a season`() {
            // Given
            val seasonId = 1L
            val stats =
                listOf(
                    createAttendanceStatResponse(id = 1L, seasonId = seasonId),
                    createAttendanceStatResponse(id = 2L, seasonId = seasonId),
                )
            val expectedResponse =
                PagedResponse(
                    content = stats,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { attendanceStatService.findBySeasonId(seasonId, any()) } returns expectedResponse

            // When
            val result = controller.findBySeasonId(seasonId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.seasonId == seasonId } shouldBe true
        }
    }

    @Nested
    inner class CountByCharacterIdTests {
        @Test
        fun `should return count for character`() {
            // Given
            val characterId = 100L
            every { attendanceStatService.countByCharacterId(characterId) } returns 5L

            // When
            val result = controller.countByCharacterId(characterId)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { attendanceStatService.countByCharacterId(characterId) }
        }
    }

    private fun createAttendanceStatResponse(
        id: Long = 1L,
        instance: String? = "Nerub-ar Palace",
        encounter: String? = "Ulgrax the Devourer",
        startDate: LocalDate? = LocalDate.of(2024, 1, 1),
        endDate: LocalDate? = LocalDate.of(2024, 1, 31),
        characterId: Long? = 100L,
        characterName: String = "Testchar",
        characterRealm: String? = "Silvermoon",
        characterClass: String? = "Warrior",
        characterRole: String? = "Tank",
        characterRegion: String? = "EU",
        attendedAmountOfRaids: Int? = 4,
        totalAmountOfRaids: Int? = 5,
        attendedPercentage: Double? = 0.80,
        selectedAmountOfEncounters: Int? = 8,
        totalAmountOfEncounters: Int? = 10,
        selectedPercentage: Double? = 0.80,
        teamId: Long? = 1L,
        seasonId: Long? = 1L,
        periodId: Long? = 1L,
        syncedAt: OffsetDateTime = OffsetDateTime.now(),
    ): AttendanceStatResponse =
        AttendanceStatResponse(
            id = id,
            instance = instance,
            encounter = encounter,
            startDate = startDate,
            endDate = endDate,
            characterId = characterId,
            characterName = characterName,
            characterRealm = characterRealm,
            characterClass = characterClass,
            characterRole = characterRole,
            characterRegion = characterRegion,
            attendedAmountOfRaids = attendedAmountOfRaids,
            totalAmountOfRaids = totalAmountOfRaids,
            attendedPercentage = attendedPercentage,
            selectedAmountOfEncounters = selectedAmountOfEncounters,
            totalAmountOfEncounters = totalAmountOfEncounters,
            selectedPercentage = selectedPercentage,
            teamId = teamId,
            seasonId = seasonId,
            periodId = periodId,
            syncedAt = syncedAt,
        )
}
