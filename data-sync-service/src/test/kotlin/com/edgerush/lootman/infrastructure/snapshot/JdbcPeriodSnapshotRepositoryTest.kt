package com.edgerush.lootman.infrastructure.snapshot

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.PeriodSnapshotEntitySpringRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Optional

/**
 * Unit tests for JdbcPeriodSnapshotRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcPeriodSnapshotRepositoryTest : UnitTest() {
    private lateinit var springRepository: PeriodSnapshotEntitySpringRepository
    private lateinit var repository: JdbcPeriodSnapshotRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcPeriodSnapshotRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return period snapshot when found`() {
            // Given
            val id = 1L
            val entity = createEntity(id = id)
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should return null when period snapshot not found`() {
            // Given
            val id = 999L
            every { springRepository.findById(id) } returns Optional.empty()

            // When
            val result = repository.findById(id)

            // Then
            result shouldBe null
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L
            val entity =
                createEntity(
                    id = id,
                    teamId = null,
                    seasonId = null,
                    periodId = null,
                    currentPeriod = null,
                )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.teamId shouldBe null
            result?.seasonId shouldBe null
            result?.periodId shouldBe null
            result?.currentPeriod shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated period snapshots`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities =
                listOf(
                    createEntity(1L, 100L),
                    createEntity(2L, 100L),
                )
            val page = PageImpl(entities)

            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
            verify { springRepository.findAll(any<Pageable>()) }
        }
    }

    @Nested
    inner class FindByTeamIdTests {
        @Test
        fun `should return snapshots for team`() {
            // Given
            val teamId = 100L
            val entities =
                listOf(
                    createEntity(1L, teamId = teamId),
                    createEntity(2L, teamId = teamId),
                )
            val page = PageImpl(entities)

            every { springRepository.findByTeamId(teamId, any<Pageable>()) } returns page

            // When
            val result = repository.findByTeamId(teamId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.teamId == teamId } shouldBe true
            verify { springRepository.findByTeamId(teamId, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when team has no snapshots`() {
            // Given
            val teamId = 999L
            val page = PageImpl(emptyList<PeriodSnapshotEntity>())

            every { springRepository.findByTeamId(teamId, any<Pageable>()) } returns page

            // When
            val result = repository.findByTeamId(teamId, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByTeamId(teamId, any<Pageable>()) }
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            // Given
            every { springRepository.count() } returns 42L

            // When
            val result = repository.count()

            // Then
            result shouldBe 42L
            verify { springRepository.count() }
        }

        @Test
        fun `should return count by team id`() {
            // Given
            val teamId = 100L
            every { springRepository.countByTeamId(teamId) } returns 3L

            // When
            val result = repository.countByTeamId(teamId)

            // Then
            result shouldBe 3L
            verify { springRepository.countByTeamId(teamId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when snapshot exists`() {
            // Given
            val id = 1L
            every { springRepository.existsById(id) } returns true

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe true
            verify { springRepository.existsById(id) }
        }

        @Test
        fun `should return false when snapshot does not exist`() {
            // Given
            val id = 999L
            every { springRepository.existsById(id) } returns false

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe false
            verify { springRepository.existsById(id) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save new entity and return saved result`() {
            // Given
            val entity = createEntity(id = null)
            val savedEntity = createEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.teamId shouldBe entity.teamId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing snapshot`() {
            // Given
            val entity = createEntity(id = 1L)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            verify { springRepository.save(entity) }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete snapshot by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createEntity(
        id: Long? = 1L,
        teamId: Long? = 100L,
        seasonId: Long? = 1L,
        periodId: Long? = 5L,
        currentPeriod: Long? = 5L,
        fetchedAt: OffsetDateTime = now,
    ) = PeriodSnapshotEntity(id, teamId, seasonId, periodId, currentPeriod, fetchedAt)
}
