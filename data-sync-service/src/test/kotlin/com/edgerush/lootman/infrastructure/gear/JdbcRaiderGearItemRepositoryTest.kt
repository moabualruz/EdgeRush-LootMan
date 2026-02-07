package com.edgerush.lootman.infrastructure.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.RaiderGearItemEntitySpringRepository
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
 * Unit tests for JdbcRaiderGearItemRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcRaiderGearItemRepositoryTest : UnitTest() {
    private lateinit var springRepository: RaiderGearItemEntitySpringRepository
    private lateinit var repository: JdbcRaiderGearItemRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcRaiderGearItemRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return gear item when found`() {
            // Given
            val id = 1L
            val entity = createGearItemEntity(id = id)
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
        fun `should return null when gear item not found`() {
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
            val entity =
                createGearItemEntity(
                    id = id,
                    raiderId = 100L,
                    gearSet = "equipped",
                    slot = "head",
                    itemId = 12345L,
                    itemLevel = 450,
                    quality = 4,
                    enchant = "Enchant",
                    enchantQuality = 3,
                    upgradeLevel = 2,
                    sockets = 1,
                    name = "Helm of Power",
                )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.gearSet shouldBe "equipped"
            result?.slot shouldBe "head"
            result?.itemId shouldBe 12345L
            result?.itemLevel shouldBe 450
            result?.quality shouldBe 4
            result?.enchant shouldBe "Enchant"
            result?.enchantQuality shouldBe 3
            result?.upgradeLevel shouldBe 2
            result?.sockets shouldBe 1
            result?.name shouldBe "Helm of Power"
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L
            val entity =
                createGearItemEntity(
                    id = id,
                    raiderId = 100L,
                    itemId = null,
                    itemLevel = null,
                    quality = null,
                    enchant = null,
                    enchantQuality = null,
                    upgradeLevel = null,
                    sockets = null,
                    name = null,
                )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.itemId shouldBe null
            result?.itemLevel shouldBe null
            result?.quality shouldBe null
            result?.enchant shouldBe null
            result?.enchantQuality shouldBe null
            result?.upgradeLevel shouldBe null
            result?.sockets shouldBe null
            result?.name shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated gear items`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities =
                listOf(
                    createGearItemEntity(1L, 100L),
                    createGearItemEntity(2L, 100L),
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
        fun `should return gear items for raider`() {
            // Given
            val raiderId = 100L
            val entities =
                listOf(
                    createGearItemEntity(1L, raiderId, slot = "head"),
                    createGearItemEntity(2L, raiderId, slot = "chest"),
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
        fun `should return empty list when raider has no gear items`() {
            // Given
            val raiderId = 999L
            val page = PageImpl(emptyList<RaiderGearItemEntity>())

            every { springRepository.findByRaiderId(raiderId, any<Pageable>()) } returns page

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByRaiderId(raiderId, any<Pageable>()) }
        }
    }

    @Nested
    inner class FindByRaiderIdAndGearSetTests {
        @Test
        fun `should return gear items for raider and gear set`() {
            // Given
            val raiderId = 100L
            val gearSet = "equipped"
            val entities =
                listOf(
                    createGearItemEntity(1L, raiderId, gearSet = gearSet, slot = "head"),
                    createGearItemEntity(2L, raiderId, gearSet = gearSet, slot = "chest"),
                )
            val page = PageImpl(entities)

            every { springRepository.findByRaiderIdAndGearSet(raiderId, gearSet, any<Pageable>()) } returns page

            // When
            val result = repository.findByRaiderIdAndGearSet(raiderId, gearSet, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId && it.gearSet == gearSet } shouldBe true
            verify { springRepository.findByRaiderIdAndGearSet(raiderId, gearSet, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when no gear items for gear set`() {
            // Given
            val raiderId = 100L
            val gearSet = "nonexistent"
            val page = PageImpl(emptyList<RaiderGearItemEntity>())

            every { springRepository.findByRaiderIdAndGearSet(raiderId, gearSet, any<Pageable>()) } returns page

            // When
            val result = repository.findByRaiderIdAndGearSet(raiderId, gearSet, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByRaiderIdAndGearSet(raiderId, gearSet, any<Pageable>()) }
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
            every { springRepository.countByRaiderId(raiderId) } returns 16L

            // When
            val result = repository.countByRaiderId(raiderId)

            // Then
            result shouldBe 16L
            verify { springRepository.countByRaiderId(raiderId) }
        }

        @Test
        fun `should return count by raider id and gear set`() {
            // Given
            val raiderId = 100L
            val gearSet = "equipped"
            every { springRepository.countByRaiderIdAndGearSet(raiderId, gearSet) } returns 16L

            // When
            val result = repository.countByRaiderIdAndGearSet(raiderId, gearSet)

            // Then
            result shouldBe 16L
            verify { springRepository.countByRaiderIdAndGearSet(raiderId, gearSet) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when gear item exists`() {
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
        fun `should return false when gear item does not exist`() {
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
            val entity = createGearItemEntity(id = null)
            val savedEntity = createGearItemEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.raiderId shouldBe entity.raiderId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing gear item`() {
            // Given
            val entity = createGearItemEntity(id = 1L)
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
        fun `should delete gear item by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createGearItemEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        gearSet: String = "equipped",
        slot: String = "head",
        itemId: Long? = 12345L,
        itemLevel: Int? = 450,
        quality: Int? = 4,
        enchant: String? = "Enchant",
        enchantQuality: Int? = 3,
        upgradeLevel: Int? = 2,
        sockets: Int? = 1,
        name: String? = "Helm",
    ): RaiderGearItemEntity =
        RaiderGearItemEntity(
            id = id,
            raiderId = raiderId,
            gearSet = gearSet,
            slot = slot,
            itemId = itemId,
            itemLevel = itemLevel,
            quality = quality,
            enchant = enchant,
            enchantQuality = enchantQuality,
            upgradeLevel = upgradeLevel,
            sockets = sockets,
            name = name,
        )
}
