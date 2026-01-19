package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.RaiderWarcraftLogEntitySpringRepository
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
import java.util.Optional

/**
 * Unit tests for JdbcRaiderWarcraftLogRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcRaiderWarcraftLogRepositoryTest : UnitTest() {
    private lateinit var springRepository: RaiderWarcraftLogEntitySpringRepository
    private lateinit var repository: JdbcRaiderWarcraftLogRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcRaiderWarcraftLogRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return warcraft log when found`() {
            // Given
            val id = 1L
            val entity = createWarcraftLogEntity(id = id, raiderId = 100L)
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should return null when warcraft log not found`() {
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
        fun `should map all database fields to entity`() {
            // Given
            val id = 1L
            val entity = createWarcraftLogEntity(
                id = id,
                raiderId = 100L,
                difficulty = "Mythic",
                score = 95,
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.difficulty shouldBe "Mythic"
            result?.score shouldBe 95
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null score`() {
            // Given
            val id = 1L
            val entity = createWarcraftLogEntity(
                id = id,
                raiderId = 100L,
                difficulty = "Heroic",
                score = null,
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.score shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated warcraft logs`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createWarcraftLogEntity(1L, 100L),
                createWarcraftLogEntity(2L, 100L),
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
    inner class FindByRaiderIdTests {
        @Test
        fun `should return warcraft logs for raider`() {
            // Given
            val raiderId = 100L
            val entities = listOf(
                createWarcraftLogEntity(1L, raiderId, difficulty = "Heroic"),
                createWarcraftLogEntity(2L, raiderId, difficulty = "Mythic"),
            )
            val page = PageImpl(entities)

            every { springRepository.findByRaiderId(raiderId, any<Pageable>()) } returns page

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
            verify { springRepository.findByRaiderId(raiderId, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when raider has no warcraft logs`() {
            // Given
            val raiderId = 999L
            val page = PageImpl(emptyList<RaiderWarcraftLogEntity>())

            every { springRepository.findByRaiderId(raiderId, any<Pageable>()) } returns page

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByRaiderId(raiderId, any<Pageable>()) }
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
        fun `should return count by raider id`() {
            // Given
            val raiderId = 100L
            every { springRepository.countByRaiderId(raiderId) } returns 3L

            // When
            val result = repository.countByRaiderId(raiderId)

            // Then
            result shouldBe 3L
            verify { springRepository.countByRaiderId(raiderId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when warcraft log exists`() {
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
        fun `should return false when warcraft log does not exist`() {
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
            val entity = createWarcraftLogEntity(id = null)
            val savedEntity = createWarcraftLogEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.raiderId shouldBe entity.raiderId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing warcraft log`() {
            // Given
            val entity = createWarcraftLogEntity(id = 1L)
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
        fun `should delete warcraft log by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createWarcraftLogEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        difficulty: String = "Heroic",
        score: Int? = 85,
    ): RaiderWarcraftLogEntity =
        RaiderWarcraftLogEntity(
            id = id,
            raiderId = raiderId,
            difficulty = difficulty,
            score = score,
        )
}
