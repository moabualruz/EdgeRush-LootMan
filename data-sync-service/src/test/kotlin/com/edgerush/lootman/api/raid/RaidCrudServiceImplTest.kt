package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.domain.raids.repository.RaidRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Unit tests for RaidCrudServiceImpl.
 *
 * Tests the service layer using mocked repository.
 */
class RaidCrudServiceImplTest : UnitTest() {
    private lateinit var raidRepository: RaidRepository
    private lateinit var service: RaidCrudServiceImpl

    @BeforeEach
    fun setup() {
        raidRepository = mockk()
        service = RaidCrudServiceImpl(raidRepository)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response`() {
            // Given
            val raids =
                listOf(
                    createRaidEntity(raidId = 1L),
                    createRaidEntity(raidId = 2L),
                )
            every { raidRepository.findAll(0L, 20) } returns raids
            every { raidRepository.count() } returns 2L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findAll(pageRequest)

            // Then
            result.content.size shouldBe 2
            result.totalElements shouldBe 2
            result.page shouldBe 0
            result.size shouldBe 20
        }

        @Test
        fun `should handle empty result`() {
            // Given
            every { raidRepository.findAll(0L, 20) } returns emptyList()
            every { raidRepository.count() } returns 0L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findAll(pageRequest)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }

        @Test
        fun `should calculate correct offset for page`() {
            // Given
            every { raidRepository.findAll(40L, 20) } returns emptyList()
            every { raidRepository.count() } returns 100L

            val pageRequest = PageRequest(page = 2, size = 20)

            // When
            service.findAll(pageRequest)

            // Then
            verify { raidRepository.findAll(40L, 20) }
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return raid when found`() {
            // Given
            val raid = createRaidEntity(raidId = 123L, instance = "Nerub-ar Palace")
            every { raidRepository.findById(123L) } returns raid

            // When
            val result = service.findById(123L)

            // Then
            result.raidId shouldBe 123L
            result.instance shouldBe "Nerub-ar Palace"
        }

        @Test
        fun `should throw exception when not found`() {
            // Given
            every { raidRepository.findById(999L) } returns null

            // When/Then
            try {
                service.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should create raid and return response`() {
            // Given
            val request =
                CreateRaidRequest(
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

            val entitySlot = slot<RaidEntity>()
            every { raidRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            val result = service.create(request)

            // Then
            result.instance shouldBe "Nerub-ar Palace"
            result.difficulty shouldBe "Mythic"
            result.date shouldBe LocalDate.of(2024, 3, 15)
            result.teamId shouldBe 1L

            entitySlot.captured.createdAt shouldNotBe null
            entitySlot.captured.updatedAt shouldNotBe null
        }

        @Test
        fun `should generate raid ID`() {
            // Given
            val request = CreateRaidRequest(instance = "Test Raid")

            val entitySlot = slot<RaidEntity>()
            every { raidRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            service.create(request)

            // Then
            entitySlot.captured.raidId shouldNotBe 0L
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should update raid and return response`() {
            // Given
            val existing = createRaidEntity(raidId = 1L, status = "SCHEDULED")
            every { raidRepository.findById(1L) } returns existing

            val request = UpdateRaidRequest(status = "COMPLETED", presentSize = 19)

            val entitySlot = slot<RaidEntity>()
            every { raidRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            val result = service.update(1L, request)

            // Then
            result.status shouldBe "COMPLETED"
            result.presentSize shouldBe 19
            entitySlot.captured.status shouldBe "COMPLETED"
            entitySlot.captured.presentSize shouldBe 19
        }

        @Test
        fun `should preserve existing values for null fields`() {
            // Given
            val existing =
                createRaidEntity(
                    raidId = 1L,
                    instance = "Nerub-ar Palace",
                    difficulty = "Mythic",
                    notes = "Original notes",
                )
            every { raidRepository.findById(1L) } returns existing

            val request = UpdateRaidRequest(status = "COMPLETED")

            val entitySlot = slot<RaidEntity>()
            every { raidRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            val result = service.update(1L, request)

            // Then
            result.instance shouldBe "Nerub-ar Palace"
            result.difficulty shouldBe "Mythic"
            result.notes shouldBe "Original notes"
            result.status shouldBe "COMPLETED"
        }

        @Test
        fun `should throw exception when raid not found`() {
            // Given
            every { raidRepository.findById(999L) } returns null
            val request = UpdateRaidRequest(status = "COMPLETED")

            // When/Then
            try {
                service.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }

        @Test
        fun `should update updatedAt timestamp`() {
            // Given
            val oldUpdatedAt = OffsetDateTime.now().minusDays(1)
            val existing = createRaidEntity(raidId = 1L).copy(updatedAt = oldUpdatedAt)
            every { raidRepository.findById(1L) } returns existing

            val request = UpdateRaidRequest(status = "COMPLETED")

            val entitySlot = slot<RaidEntity>()
            every { raidRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            service.update(1L, request)

            // Then
            entitySlot.captured.updatedAt shouldNotBe oldUpdatedAt
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete raid when exists`() {
            // Given
            every { raidRepository.existsById(1L) } returns true
            every { raidRepository.delete(1L) } returns Unit

            // When
            service.delete(1L)

            // Then
            verify { raidRepository.delete(1L) }
        }

        @Test
        fun `should throw exception when raid not found`() {
            // Given
            every { raidRepository.existsById(999L) } returns false

            // When/Then
            try {
                service.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raid not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when raid exists`() {
            // Given
            every { raidRepository.existsById(1L) } returns true

            // When
            val result = service.existsById(1L)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when raid does not exist`() {
            // Given
            every { raidRepository.existsById(999L) } returns false

            // When
            val result = service.existsById(999L)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class FindByTeamTests {
        @Test
        fun `should return paged raids for team`() {
            // Given
            val teamId = 1L
            val raids =
                listOf(
                    createRaidEntity(raidId = 1L, teamId = teamId),
                    createRaidEntity(raidId = 2L, teamId = teamId),
                )
            every { raidRepository.findByTeamId(teamId, 0L, 20) } returns raids
            every { raidRepository.countByTeamId(teamId) } returns 2L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findByTeam(teamId, pageRequest)

            // Then
            result.content.size shouldBe 2
            result.totalElements shouldBe 2
            result.content.all { it.teamId == teamId } shouldBe true
        }

        @Test
        fun `should return empty response for team with no raids`() {
            // Given
            val teamId = 999L
            every { raidRepository.findByTeamId(teamId, 0L, 20) } returns emptyList()
            every { raidRepository.countByTeamId(teamId) } returns 0L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findByTeam(teamId, pageRequest)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }
    }

    @Nested
    inner class FindByDateRangeTests {
        @Test
        fun `should return raids within date range`() {
            // Given
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 3, 31)
            val raids =
                listOf(
                    createRaidEntity(raidId = 1L, date = LocalDate.of(2024, 1, 15)),
                    createRaidEntity(raidId = 2L, date = LocalDate.of(2024, 2, 20)),
                )
            every { raidRepository.findByDateRange(startDate, endDate, 0L, 20) } returns raids
            every { raidRepository.countByDateRange(startDate, endDate) } returns 2L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findByDateRange(startDate, endDate, pageRequest)

            // Then
            result.content.size shouldBe 2
            result.totalElements shouldBe 2
        }

        @Test
        fun `should return empty response for date range with no raids`() {
            // Given
            val startDate = LocalDate.of(2020, 1, 1)
            val endDate = LocalDate.of(2020, 12, 31)
            every { raidRepository.findByDateRange(startDate, endDate, 0L, 20) } returns emptyList()
            every { raidRepository.countByDateRange(startDate, endDate) } returns 0L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findByDateRange(startDate, endDate, pageRequest)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }
    }

    @Nested
    inner class CountByTeamTests {
        @Test
        fun `should return count for team`() {
            // Given
            val teamId = 1L
            every { raidRepository.countByTeamId(teamId) } returns 42L

            // When
            val result = service.countByTeam(teamId)

            // Then
            result shouldBe 42L
        }

        @Test
        fun `should return zero for team with no raids`() {
            // Given
            every { raidRepository.countByTeamId(999L) } returns 0L

            // When
            val result = service.countByTeam(999L)

            // Then
            result shouldBe 0L
        }
    }

    private fun createRaidEntity(
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
    ): RaidEntity =
        RaidEntity(
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
