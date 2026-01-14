package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.domain.raids.repository.RaidEncounterRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for RaidEncounterCrudServiceImpl.
 *
 * Tests the service layer using mocked repository.
 */
class RaidEncounterCrudServiceImplTest : UnitTest() {

    private lateinit var encounterRepository: RaidEncounterRepository
    private lateinit var service: RaidEncounterCrudServiceImpl

    @BeforeEach
    fun setup() {
        encounterRepository = mockk()
        service = RaidEncounterCrudServiceImpl(encounterRepository)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response`() {
            // Given
            val encounters = listOf(
                createEncounterEntity(id = 1L),
                createEncounterEntity(id = 2L),
            )
            every { encounterRepository.findAll(0L, 20) } returns encounters
            every { encounterRepository.count() } returns 2L

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
            every { encounterRepository.findAll(0L, 20) } returns emptyList()
            every { encounterRepository.count() } returns 0L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findAll(pageRequest)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return encounter when found`() {
            // Given
            val encounter = createEncounterEntity(id = 123L, name = "Queen Ansurek")
            every { encounterRepository.findById(123L) } returns encounter

            // When
            val result = service.findById(123L)

            // Then
            result.id shouldBe 123L
            result.name shouldBe "Queen Ansurek"
        }

        @Test
        fun `should throw exception when not found`() {
            // Given
            every { encounterRepository.findById(999L) } returns null

            // When/Then
            try {
                service.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should create encounter and return response`() {
            // Given
            val request = CreateRaidEncounterRequest(
                raidId = 1L,
                encounterId = 2902L,
                name = "Queen Ansurek",
                enabled = true,
                extra = false,
            )

            val entitySlot = slot<RaidEncounterEntity>()
            every { encounterRepository.save(capture(entitySlot)) } answers {
                entitySlot.captured.copy(id = 42L)
            }

            // When
            val result = service.create(request)

            // Then
            result.id shouldBe 42L
            result.raidId shouldBe 1L
            result.name shouldBe "Queen Ansurek"
            result.encounterId shouldBe 2902L

            entitySlot.captured.id shouldBe null
            entitySlot.captured.raidId shouldBe 1L
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should update encounter and return response`() {
            // Given
            val existing = createEncounterEntity(id = 1L, enabled = true)
            every { encounterRepository.findById(1L) } returns existing

            val request = UpdateRaidEncounterRequest(enabled = false, notes = "Skipping")

            val entitySlot = slot<RaidEncounterEntity>()
            every { encounterRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            val result = service.update(1L, request)

            // Then
            result.enabled shouldBe false
            result.notes shouldBe "Skipping"
            entitySlot.captured.enabled shouldBe false
            entitySlot.captured.notes shouldBe "Skipping"
        }

        @Test
        fun `should preserve existing values for null fields`() {
            // Given
            val existing = createEncounterEntity(
                id = 1L,
                name = "Queen Ansurek",
                encounterId = 2902L,
                notes = "Original notes",
            )
            every { encounterRepository.findById(1L) } returns existing

            val request = UpdateRaidEncounterRequest(enabled = false)

            val entitySlot = slot<RaidEncounterEntity>()
            every { encounterRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

            // When
            val result = service.update(1L, request)

            // Then
            result.name shouldBe "Queen Ansurek"
            result.encounterId shouldBe 2902L
            result.notes shouldBe "Original notes"
            result.enabled shouldBe false
        }

        @Test
        fun `should throw exception when encounter not found`() {
            // Given
            every { encounterRepository.findById(999L) } returns null
            val request = UpdateRaidEncounterRequest(enabled = false)

            // When/Then
            try {
                service.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete encounter when exists`() {
            // Given
            every { encounterRepository.existsById(1L) } returns true
            every { encounterRepository.delete(1L) } returns Unit

            // When
            service.delete(1L)

            // Then
            verify { encounterRepository.delete(1L) }
        }

        @Test
        fun `should throw exception when encounter not found`() {
            // Given
            every { encounterRepository.existsById(999L) } returns false

            // When/Then
            try {
                service.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Encounter not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when encounter exists`() {
            // Given
            every { encounterRepository.existsById(1L) } returns true

            // When
            val result = service.existsById(1L)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when encounter does not exist`() {
            // Given
            every { encounterRepository.existsById(999L) } returns false

            // When
            val result = service.existsById(999L)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class FindByRaidTests {

        @Test
        fun `should return paged encounters for raid`() {
            // Given
            val raidId = 1L
            val encounters = listOf(
                createEncounterEntity(id = 1L, raidId = raidId),
                createEncounterEntity(id = 2L, raidId = raidId),
            )
            every { encounterRepository.findByRaidId(raidId, 0L, 20) } returns encounters
            every { encounterRepository.countByRaidId(raidId) } returns 2L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findByRaid(raidId, pageRequest)

            // Then
            result.content.size shouldBe 2
            result.totalElements shouldBe 2
            result.content.all { it.raidId == raidId } shouldBe true
        }
    }

    @Nested
    inner class FindEnabledByRaidTests {

        @Test
        fun `should return only enabled encounters`() {
            // Given
            val raidId = 1L
            val encounters = listOf(
                createEncounterEntity(id = 1L, raidId = raidId, enabled = true),
                createEncounterEntity(id = 2L, raidId = raidId, enabled = true),
            )
            every { encounterRepository.findEnabledByRaidId(raidId, 0L, 20) } returns encounters
            every { encounterRepository.countEnabledByRaidId(raidId) } returns 2L

            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val result = service.findEnabledByRaid(raidId, pageRequest)

            // Then
            result.content.size shouldBe 2
            result.content.all { it.enabled == true } shouldBe true
        }
    }

    @Nested
    inner class CountByRaidTests {

        @Test
        fun `should return count for raid`() {
            // Given
            val raidId = 1L
            every { encounterRepository.countByRaidId(raidId) } returns 8L

            // When
            val result = service.countByRaid(raidId)

            // Then
            result shouldBe 8L
        }
    }

    private fun createEncounterEntity(
        id: Long? = 1L,
        raidId: Long = 1L,
        encounterId: Long? = 2900L,
        name: String? = "Test Encounter",
        enabled: Boolean? = true,
        extra: Boolean? = false,
        notes: String? = null,
    ): RaidEncounterEntity = RaidEncounterEntity(
        id = id,
        raidId = raidId,
        encounterId = encounterId,
        name = name,
        enabled = enabled,
        extra = extra,
        notes = notes,
    )
}
