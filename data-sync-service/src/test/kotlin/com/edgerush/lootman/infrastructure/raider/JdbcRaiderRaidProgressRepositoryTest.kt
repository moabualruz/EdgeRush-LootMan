package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.RaiderRaidProgressEntitySpringRepository
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
 * Unit tests for JdbcRaiderRaidProgressRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcRaiderRaidProgressRepositoryTest : UnitTest() {
    private lateinit var springRepository: RaiderRaidProgressEntitySpringRepository
    private lateinit var repository: JdbcRaiderRaidProgressRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcRaiderRaidProgressRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return raid progress when found`() {
            // Given
            val id = 1L
            val entity = createRaidProgressEntity(id = id)
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
        fun `should return null when raid progress not found`() {
            // Given
            val id = 999L
            every { springRepository.findById(id) } returns Optional.empty()

            // When
            val result = repository.findById(id)

            // Then
            result shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated raid progress`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities =
                listOf(
                    createRaidProgressEntity(1L, 100L),
                    createRaidProgressEntity(2L, 100L),
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
        fun `should return raid progress for raider`() {
            // Given
            val raiderId = 100L
            val entities =
                listOf(
                    createRaidProgressEntity(1L, raiderId, difficulty = "Heroic"),
                    createRaidProgressEntity(2L, raiderId, difficulty = "Mythic"),
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
        fun `should return empty list when raider has no raid progress`() {
            // Given
            val raiderId = 999L
            val page = PageImpl(emptyList<RaiderRaidProgressEntity>())

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
            every { springRepository.countByRaiderId(raiderId) } returns 4L

            // When
            val result = repository.countByRaiderId(raiderId)

            // Then
            result shouldBe 4L
            verify { springRepository.countByRaiderId(raiderId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when raid progress exists`() {
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
        fun `should return false when raid progress does not exist`() {
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
        fun `should save entity and return saved result`() {
            // Given
            val entity = createRaidProgressEntity(id = null)
            val savedEntity = createRaidProgressEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.raiderId shouldBe entity.raiderId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing raid progress`() {
            // Given
            val entity = createRaidProgressEntity(id = 1L)
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
        fun `should delete raid progress by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createRaidProgressEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        raid: String = "Nerub-ar Palace",
        difficulty: String = "Heroic",
        bossesDefeated: Int? = 8,
    ): RaiderRaidProgressEntity =
        RaiderRaidProgressEntity(
            id = id,
            raiderId = raiderId,
            raid = raid,
            difficulty = difficulty,
            bossesDefeated = bossesDefeated,
        )
}
