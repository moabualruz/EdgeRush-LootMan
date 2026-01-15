package com.edgerush.lootman.api.statistics

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

class RaiderStatisticsControllerTest : UnitTest() {
    private lateinit var service: RaiderStatisticsCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaiderStatisticsController

    @BeforeEach
    fun setup() {
        service = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaiderStatisticsController(service, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response`() {
            val expected = PagedResponse(listOf(createResponse(id = 1L)), 0, 20, 1)
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
            val expected = createResponse(id = 1L)
            every { service.findById(1L) } returns expected
            controller.findById(1L).id shouldBe 1L
        }

        @Test
        fun `should create with 201`() {
            val request = CreateRaiderStatisticsRequest(raiderId = 1L)
            every { service.create(request) } returns createResponse(id = 1L)
            controller.create(request).statusCode shouldBe HttpStatus.CREATED
        }

        @Test
        fun `should update`() {
            val request = UpdateRaiderStatisticsRequest(mythicPlusScore = 2500.0)
            every { service.update(1L, request) } returns createResponse(id = 1L, mythicPlusScore = 2500.0)
            controller.update(1L, request).id shouldBe 1L
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
        fun `should return statistics for raider`() {
            every { service.findByRaiderId(1L) } returns createResponse()
            controller.findByRaiderId(1L).raiderId shouldBe 1L
        }
    }

    private fun createResponse(
        id: Long = 1L,
        raiderId: Long = 1L,
        mythicPlusScore: Double? = 2500.0,
        weeklyHighestMplus: Int? = 15,
        seasonHighestMplus: Int? = 20,
        worldQuestsTotal: Int? = 1000,
        worldQuestsThisWeek: Int? = 10,
        collectiblesMounts: Int? = 200,
        collectiblesToys: Int? = 300,
        collectiblesUniquePets: Int? = 150,
        collectiblesLevel25Pets: Int? = 50,
        honorLevel: Int? = 100,
    ) = RaiderStatisticsResponse(
        id, raiderId, mythicPlusScore, weeklyHighestMplus, seasonHighestMplus,
        worldQuestsTotal, worldQuestsThisWeek, collectiblesMounts, collectiblesToys,
        collectiblesUniquePets, collectiblesLevel25Pets, honorLevel,
    )
}
