package com.edgerush.lootman.api.activity

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
import java.time.OffsetDateTime

class HistoricalActivityControllerTest : UnitTest() {

    private lateinit var service: HistoricalActivityCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: HistoricalActivityController

    @BeforeEach
    fun setup() {
        service = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = HistoricalActivityController(service, paginationProperties)
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
            val request = CreateHistoricalActivityRequest(characterId = 1L, characterName = "Test", dataJson = "{}")
            every { service.create(request) } returns createResponse(id = 1L)
            controller.create(request).statusCode shouldBe HttpStatus.CREATED
        }

        @Test
        fun `should update`() {
            val request = UpdateHistoricalActivityRequest(dataJson = "{\"updated\": true}")
            every { service.update(1L, request) } returns createResponse(id = 1L)
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
    inner class FindByCharacterIdTests {
        @Test
        fun `should return activities for character`() {
            every { service.findByCharacterId(1L, any()) } returns PagedResponse(listOf(createResponse()), 0, 20, 1)
            controller.findByCharacterId(1L, 0, null).totalElements shouldBe 1
        }
    }

    @Nested
    inner class FindByTeamIdTests {
        @Test
        fun `should return activities for team`() {
            every { service.findByTeamId(1L, any()) } returns PagedResponse(listOf(createResponse()), 0, 20, 1)
            controller.findByTeamId(1L, 0, null).totalElements shouldBe 1
        }
    }

    private fun createResponse(
        id: Long = 1L,
        characterId: Long? = 1L,
        characterName: String = "TestChar",
        characterRealm: String? = "TestRealm",
        periodId: Long? = 1L,
        teamId: Long? = 1L,
        seasonId: Long? = 1L,
        dataJson: String = "{}",
        syncedAt: OffsetDateTime = OffsetDateTime.now()
    ) = HistoricalActivityResponse(id, characterId, characterName, characterRealm, periodId, teamId, seasonId, dataJson, syncedAt)
}
