package com.edgerush.lootman.infrastructure.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.RaiderStatisticsEntitySpringRepository
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
 * Unit tests for JdbcRaiderStatisticsRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcRaiderStatisticsRepositoryTest : UnitTest() {
    private lateinit var springRepository: RaiderStatisticsEntitySpringRepository
    private lateinit var repository: JdbcRaiderStatisticsRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcRaiderStatisticsRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return statistics when found`() {
            // Given
            val id = 1L
            val entity = createStatisticsEntity(id = id)
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
        fun `should return null when statistics not found`() {
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
                createStatisticsEntity(
                    id = id,
                    raiderId = 100L,
                    mythicPlusScore = 2500.5,
                    weeklyHighestMplus = 20,
                    seasonHighestMplus = 22,
                    worldQuestsTotal = 1000,
                    worldQuestsThisWeek = 50,
                    collectiblesMounts = 300,
                    collectiblesToys = 150,
                    collectiblesUniquePets = 200,
                    collectiblesLevel25Pets = 50,
                    honorLevel = 100,
                )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.mythicPlusScore shouldBe 2500.5
            result?.weeklyHighestMplus shouldBe 20
            result?.seasonHighestMplus shouldBe 22
            result?.worldQuestsTotal shouldBe 1000
            result?.worldQuestsThisWeek shouldBe 50
            result?.collectiblesMounts shouldBe 300
            result?.collectiblesToys shouldBe 150
            result?.collectiblesUniquePets shouldBe 200
            result?.collectiblesLevel25Pets shouldBe 50
            result?.honorLevel shouldBe 100
            verify { springRepository.findById(id) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L
            val entity =
                createStatisticsEntity(
                    id = id,
                    raiderId = 100L,
                    mythicPlusScore = null,
                    weeklyHighestMplus = null,
                    seasonHighestMplus = null,
                    worldQuestsTotal = null,
                    worldQuestsThisWeek = null,
                    collectiblesMounts = null,
                    collectiblesToys = null,
                    collectiblesUniquePets = null,
                    collectiblesLevel25Pets = null,
                    honorLevel = null,
                )
            every { springRepository.findById(id) } returns Optional.of(entity)

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.mythicPlusScore shouldBe null
            result?.weeklyHighestMplus shouldBe null
            result?.seasonHighestMplus shouldBe null
            result?.worldQuestsTotal shouldBe null
            result?.worldQuestsThisWeek shouldBe null
            result?.collectiblesMounts shouldBe null
            result?.collectiblesToys shouldBe null
            result?.collectiblesUniquePets shouldBe null
            result?.collectiblesLevel25Pets shouldBe null
            result?.honorLevel shouldBe null
            verify { springRepository.findById(id) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated statistics`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities =
                listOf(
                    createStatisticsEntity(1L, 100L),
                    createStatisticsEntity(2L, 101L),
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
        fun `should return statistics for raider`() {
            // Given
            val raiderId = 100L
            val entity = createStatisticsEntity(1L, raiderId)
            every { springRepository.findByRaiderId(raiderId) } returns entity

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            result?.raiderId shouldBe raiderId
            verify { springRepository.findByRaiderId(raiderId) }
        }

        @Test
        fun `should return null when raider has no statistics`() {
            // Given
            val raiderId = 999L
            every { springRepository.findByRaiderId(raiderId) } returns null

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldBe null
            verify { springRepository.findByRaiderId(raiderId) }
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
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when statistics exists`() {
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
        fun `should return false when statistics does not exist`() {
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
    inner class ExistsByRaiderIdTests {
        @Test
        fun `should return true when statistics exist for raider`() {
            // Given
            val raiderId = 100L
            every { springRepository.existsByRaiderId(raiderId) } returns true

            // When
            val result = repository.existsByRaiderId(raiderId)

            // Then
            result shouldBe true
            verify { springRepository.existsByRaiderId(raiderId) }
        }

        @Test
        fun `should return false when statistics do not exist for raider`() {
            // Given
            val raiderId = 999L
            every { springRepository.existsByRaiderId(raiderId) } returns false

            // When
            val result = repository.existsByRaiderId(raiderId)

            // Then
            result shouldBe false
            verify { springRepository.existsByRaiderId(raiderId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save entity and return saved result`() {
            // Given
            val entity = createStatisticsEntity(id = null)
            val savedEntity = createStatisticsEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.raiderId shouldBe entity.raiderId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing statistics`() {
            // Given
            val entity = createStatisticsEntity(id = 1L)
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
        fun `should delete statistics by id`() {
            // Given
            val id = 1L

            // When
            repository.delete(id)

            // Then
            verify { springRepository.deleteById(id) }
        }
    }

    // Helper methods

    private fun createStatisticsEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        mythicPlusScore: Double? = 2500.0,
        weeklyHighestMplus: Int? = 20,
        seasonHighestMplus: Int? = 22,
        worldQuestsTotal: Int? = 1000,
        worldQuestsThisWeek: Int? = 50,
        collectiblesMounts: Int? = 300,
        collectiblesToys: Int? = 150,
        collectiblesUniquePets: Int? = 200,
        collectiblesLevel25Pets: Int? = 50,
        honorLevel: Int? = 100,
    ): RaiderStatisticsEntity =
        RaiderStatisticsEntity(
            id = id,
            raiderId = raiderId,
            mythicPlusScore = mythicPlusScore,
            weeklyHighestMplus = weeklyHighestMplus,
            seasonHighestMplus = seasonHighestMplus,
            worldQuestsTotal = worldQuestsTotal,
            worldQuestsThisWeek = worldQuestsThisWeek,
            collectiblesMounts = collectiblesMounts,
            collectiblesToys = collectiblesToys,
            collectiblesUniquePets = collectiblesUniquePets,
            collectiblesLevel25Pets = collectiblesLevel25Pets,
            honorLevel = honorLevel,
        )
}
