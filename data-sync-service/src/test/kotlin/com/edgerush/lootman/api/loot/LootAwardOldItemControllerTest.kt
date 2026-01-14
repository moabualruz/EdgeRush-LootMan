package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LootAwardOldItemControllerTest : UnitTest() {
    private lateinit var service: LootAwardOldItemCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: LootAwardOldItemController

    @BeforeEach
    fun setup() {
        service = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = LootAwardOldItemController(service, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response`() {
            val expected = PagedResponse(listOf(createResponse()), 0, 20, 1)
            every { service.findAll(any()) } returns expected
            controller.findAll(0, null) shouldBe expected
        }

        @Test
        fun `should cap page size`() {
            val slot = slot<PageRequest>()
            every { service.findAll(capture(slot)) } returns PagedResponse(emptyList(), 0, 100, 0)
            controller.findAll(0, 500)
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class CrudTests {
        @Test
        fun `should find by id`() {
            every { service.findById(1L) } returns createResponse()
            controller.findById(1L).id shouldBe 1L
        }

        @Test
        fun `should create with 201`() {
            every { service.create(any()) } returns createResponse()
            controller.create(CreateLootAwardOldItemRequest(1L)).statusCode shouldBe HttpStatus.CREATED
        }

        @Test
        fun `should update`() {
            every { service.update(1L, any()) } returns createResponse()
            controller.update(1L, UpdateLootAwardOldItemRequest()).id shouldBe 1L
        }

        @Test
        fun `should delete with 204`() {
            every { service.delete(1L) } returns Unit
            controller.delete(1L).statusCode shouldBe HttpStatus.NO_CONTENT
        }

        @Test
        fun `should check exists`() {
            every { service.existsById(1L) } returns true
            controller.exists(1L).exists shouldBe true
        }
    }

    @Nested
    inner class FindByLootAwardIdTests {
        @Test
        fun `should return old items for loot award`() {
            every { service.findByLootAwardId(1L, any()) } returns PagedResponse(listOf(createResponse()), 0, 20, 1)
            controller.findByLootAwardId(1L, 0, null).totalElements shouldBe 1
        }
    }

    private fun createResponse(
        id: Long = 1L,
        lootAwardId: Long = 1L,
        itemId: Long? = 12345L,
        bonusId: String? = "567"
    ) = LootAwardOldItemResponse(id, lootAwardId, itemId, bonusId)
}
