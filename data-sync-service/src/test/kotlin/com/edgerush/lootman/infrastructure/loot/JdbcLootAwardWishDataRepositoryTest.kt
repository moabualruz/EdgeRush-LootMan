package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.LootAwardWishDataEntitySpringRepository
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
 * Unit tests for JdbcLootAwardWishDataRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcLootAwardWishDataRepositoryTest : UnitTest() {
    private lateinit var springRepository: LootAwardWishDataEntitySpringRepository
    private lateinit var repository: JdbcLootAwardWishDataRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcLootAwardWishDataRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return wish data when found`() {
            // Given
            val id = 1L
            val entity = createWishDataEntity(id = id)
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should return null when wish data not found`() {
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
        fun `should map all entity fields correctly`() {
            // Given
            val id = 1L
            val entity = createWishDataEntity(
                id = id,
                lootAwardId = 100L,
                specName = "Frost",
                specIcon = "spell_deathknight_frostpresence",
                value = 5,
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
            result?.specName shouldBe "Frost"
            result?.specIcon shouldBe "spell_deathknight_frostpresence"
            result?.value shouldBe 5
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L
            val entity = createWishDataEntity(
                id = id,
                lootAwardId = 100L,
                specName = null,
                specIcon = null,
                value = null,
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.specName shouldBe null
            result?.specIcon shouldBe null
            result?.value shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated wish data`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createWishDataEntity(1L, 100L),
                createWishDataEntity(2L, 100L),
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
    inner class FindByLootAwardIdTests {
        @Test
        fun `should return wish data for loot award`() {
            // Given
            val lootAwardId = 100L
            val entities = listOf(
                createWishDataEntity(1L, lootAwardId, specName = "Frost"),
                createWishDataEntity(2L, lootAwardId, specName = "Unholy"),
            )
            val page = PageImpl(entities)

            every { springRepository.findByLootAwardId(lootAwardId, any<Pageable>()) } returns page

            // When
            val result = repository.findByLootAwardId(lootAwardId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.lootAwardId == lootAwardId } shouldBe true
            verify { springRepository.findByLootAwardId(lootAwardId, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when loot award has no wish data`() {
            // Given
            val lootAwardId = 999L
            val page = PageImpl(emptyList<LootAwardWishDataEntity>())

            every { springRepository.findByLootAwardId(lootAwardId, any<Pageable>()) } returns page

            // When
            val result = repository.findByLootAwardId(lootAwardId, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByLootAwardId(lootAwardId, any<Pageable>()) }
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
        fun `should return count by loot award id`() {
            // Given
            val lootAwardId = 100L
            every { springRepository.countByLootAwardId(lootAwardId) } returns 3L

            // When
            val result = repository.countByLootAwardId(lootAwardId)

            // Then
            result shouldBe 3L
            verify { springRepository.countByLootAwardId(lootAwardId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when wish data exists`() {
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
        fun `should return false when wish data does not exist`() {
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
            val entity = createWishDataEntity(id = null)
            val savedEntity = createWishDataEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.lootAwardId shouldBe entity.lootAwardId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing wish data`() {
            // Given
            val entity = createWishDataEntity(id = 1L)
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
        fun `should delete wish data by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createWishDataEntity(
        id: Long? = 1L,
        lootAwardId: Long = 100L,
        specName: String? = "Frost",
        specIcon: String? = "spell_icon",
        value: Int? = 5,
    ): LootAwardWishDataEntity =
        LootAwardWishDataEntity(
            id = id,
            lootAwardId = lootAwardId,
            specName = specName,
            specIcon = specIcon,
            value = value,
        )
}
