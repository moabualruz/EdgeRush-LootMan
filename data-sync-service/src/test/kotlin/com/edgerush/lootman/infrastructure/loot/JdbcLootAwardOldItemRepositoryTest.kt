package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.LootAwardOldItemEntitySpringRepository
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
 * Unit tests for JdbcLootAwardOldItemRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcLootAwardOldItemRepositoryTest : UnitTest() {
    private lateinit var springRepository: LootAwardOldItemEntitySpringRepository
    private lateinit var repository: JdbcLootAwardOldItemRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcLootAwardOldItemRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return old item when found`() {
            // Given
            val id = 1L
            val entity = createOldItemEntity(id = id)
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
        fun `should return null when old item not found`() {
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
            val entity = createOldItemEntity(
                id = id,
                lootAwardId = 100L,
                itemId = 12345L,
                bonusId = "6652",
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
            result?.itemId shouldBe 12345L
            result?.bonusId shouldBe "6652"
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null item id and bonus id`() {
            // Given
            val id = 1L
            val entity = createOldItemEntity(
                id = id,
                lootAwardId = 100L,
                itemId = null,
                bonusId = null,
            )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.itemId shouldBe null
            result?.bonusId shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated old items`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createOldItemEntity(1L, 100L),
                createOldItemEntity(2L, 100L),
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
        fun `should return old items for loot award`() {
            // Given
            val lootAwardId = 100L
            val entities = listOf(
                createOldItemEntity(1L, lootAwardId, itemId = 12345L),
                createOldItemEntity(2L, lootAwardId, itemId = 12346L),
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
        fun `should return empty list when loot award has no old items`() {
            // Given
            val lootAwardId = 999L
            val page = PageImpl(emptyList<LootAwardOldItemEntity>())

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
        fun `should return true when old item exists`() {
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
        fun `should return false when old item does not exist`() {
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
            val entity = createOldItemEntity(id = null)
            val savedEntity = createOldItemEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.lootAwardId shouldBe entity.lootAwardId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing old item`() {
            // Given
            val entity = createOldItemEntity(id = 1L)
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
        fun `should delete old item by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createOldItemEntity(
        id: Long? = 1L,
        lootAwardId: Long = 100L,
        itemId: Long? = 12345L,
        bonusId: String? = "6652",
    ): LootAwardOldItemEntity =
        LootAwardOldItemEntity(
            id = id,
            lootAwardId = lootAwardId,
            itemId = itemId,
            bonusId = bonusId,
        )
}
