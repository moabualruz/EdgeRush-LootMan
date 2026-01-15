package com.edgerush.lootman.api.raider

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

/**
 * Unit tests for RaiderVaultSlotController.
 */
class RaiderVaultSlotControllerTest : UnitTest() {
    private lateinit var vaultSlotService: RaiderVaultSlotCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaiderVaultSlotController

    @BeforeEach
    fun setup() {
        vaultSlotService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaiderVaultSlotController(vaultSlotService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createVaultSlotResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { vaultSlotService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                vaultSlotService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<RaiderVaultSlotResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { vaultSlotService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return vault slot when found`() {
            // Given
            val expected = createVaultSlotResponse(id = 123L, slot = "RAID_1")
            every { vaultSlotService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.slot shouldBe "RAID_1"
            verify(exactly = 1) { vaultSlotService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { vaultSlotService.findById(999L) } throws NoSuchElementException("Vault slot not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Vault slot not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created vault slot with 201 status`() {
            // Given
            val request =
                CreateRaiderVaultSlotRequest(
                    raiderId = 1L,
                    slot = "RAID_1",
                    unlocked = true,
                )

            val created =
                createVaultSlotResponse(
                    id = 1L,
                    raiderId = 1L,
                    slot = "RAID_1",
                    unlocked = true,
                )
            every { vaultSlotService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.slot shouldBe "RAID_1"
            result.body?.unlocked shouldBe true
            verify(exactly = 1) { vaultSlotService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated vault slot`() {
            // Given
            val request =
                UpdateRaiderVaultSlotRequest(
                    unlocked = false,
                )

            val updated =
                createVaultSlotResponse(
                    id = 1L,
                    unlocked = false,
                )
            every { vaultSlotService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.unlocked shouldBe false
            verify(exactly = 1) { vaultSlotService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when vault slot not found`() {
            // Given
            val request = UpdateRaiderVaultSlotRequest(unlocked = false)

            every { vaultSlotService.update(999L, request) } throws NoSuchElementException("Vault slot not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Vault slot not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { vaultSlotService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { vaultSlotService.delete(1L) }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when vault slot exists`() {
            // Given
            every { vaultSlotService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { vaultSlotService.existsById(1L) }
        }

        @Test
        fun `should return exists false when vault slot does not exist`() {
            // Given
            every { vaultSlotService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByRaiderTests {
        @Test
        fun `should return vault slots for a raider`() {
            // Given
            val raiderId = 1L
            val slots =
                listOf(
                    createVaultSlotResponse(id = 1L, raiderId = raiderId, slot = "RAID_1"),
                    createVaultSlotResponse(id = 2L, raiderId = raiderId, slot = "RAID_2"),
                    createVaultSlotResponse(id = 3L, raiderId = raiderId, slot = "RAID_3"),
                )
            val expectedResponse =
                PagedResponse(
                    content = slots,
                    page = 0,
                    size = 20,
                    totalElements = 3,
                )
            every { vaultSlotService.findByRaider(raiderId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaider(raiderId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 3
            result.content.all { it.raiderId == raiderId } shouldBe true
        }
    }

    @Nested
    inner class FindUnlockedByRaiderTests {
        @Test
        fun `should return only unlocked vault slots for a raider`() {
            // Given
            val raiderId = 1L
            val slots =
                listOf(
                    createVaultSlotResponse(id = 1L, raiderId = raiderId, slot = "RAID_1", unlocked = true),
                    createVaultSlotResponse(id = 2L, raiderId = raiderId, slot = "RAID_2", unlocked = true),
                )
            val expectedResponse =
                PagedResponse(
                    content = slots,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { vaultSlotService.findUnlockedByRaider(raiderId, any()) } returns expectedResponse

            // When
            val result = controller.findUnlockedByRaider(raiderId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.unlocked == true } shouldBe true
        }
    }

    @Nested
    inner class CountByRaiderTests {
        @Test
        fun `should return count for raider`() {
            // Given
            val raiderId = 1L
            every { vaultSlotService.countByRaider(raiderId) } returns 3L

            // When
            val result = controller.countByRaider(raiderId)

            // Then
            result.count shouldBe 3L
            verify(exactly = 1) { vaultSlotService.countByRaider(raiderId) }
        }
    }

    private fun createVaultSlotResponse(
        id: Long = 1L,
        raiderId: Long = 1L,
        slot: String = "RAID_1",
        unlocked: Boolean? = true,
    ): RaiderVaultSlotResponse =
        RaiderVaultSlotResponse(
            id = id,
            raiderId = raiderId,
            slot = slot,
            unlocked = unlocked,
        )
}
