package com.edgerush.lootman.api.raider

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

class RaiderWarcraftLogControllerTest : UnitTest() {
    private lateinit var service: RaiderWarcraftLogCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaiderWarcraftLogController

    @BeforeEach
    fun setup() {
        service = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaiderWarcraftLogController(service, paginationProperties)
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
            controller.create(CreateRaiderWarcraftLogRequest(1L, "mythic")).statusCode shouldBe HttpStatus.CREATED
        }

        @Test
        fun `should update`() {
            every { service.update(1L, any()) } returns createResponse()
            controller.update(1L, UpdateRaiderWarcraftLogRequest()).id shouldBe 1L
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
    inner class FindByRaiderIdTests {
        @Test
        fun `should return warcraft logs for raider`() {
            every { service.findByRaiderId(1L, any()) } returns PagedResponse(listOf(createResponse()), 0, 20, 1)
            controller.findByRaiderId(1L, 0, null).totalElements shouldBe 1
        }
    }

    private fun createResponse(
        id: Long = 1L,
        raiderId: Long = 1L,
        difficulty: String = "mythic",
        score: Int? = 95,
    ) = RaiderWarcraftLogResponse(id, raiderId, difficulty, score)
}
