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
import java.time.OffsetDateTime

/**
 * Unit tests for RaiderEntityController.
 */
class RaiderEntityControllerTest : UnitTest() {
    private lateinit var raiderEntityService: RaiderEntityCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: RaiderEntityController

    @BeforeEach
    fun setup() {
        raiderEntityService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = RaiderEntityController(raiderEntityService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            val expectedResponse =
                PagedResponse(
                    content = listOf(createRaiderEntityResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { raiderEntityService.findAll(any()) } returns expectedResponse

            val result = controller.findAll(page = 0, size = null)

            result shouldBe expectedResponse
            verify { raiderEntityService.findAll(match { it.page == 0 && it.size == 20 }) }
        }

        @Test
        fun `should cap page size at max`() {
            val slot = slot<PageRequest>()
            every { raiderEntityService.findAll(capture(slot)) } returns PagedResponse(emptyList(), 0, 100, 0)

            controller.findAll(page = 0, size = 500)

            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return raider when found`() {
            val expected = createRaiderEntityResponse(id = 123L, characterName = "Testchar")
            every { raiderEntityService.findById(123L) } returns expected

            val result = controller.findById(123L)

            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
        }

        @Test
        fun `should propagate exception when not found`() {
            every { raiderEntityService.findById(999L) } throws NoSuchElementException("Raider not found with id: 999")

            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raider not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created raider with 201 status`() {
            val request =
                CreateRaiderEntityRequest(
                    characterName = "Testchar",
                    realm = "Silvermoon",
                    region = "EU",
                    clazz = "Warrior",
                    spec = "Protection",
                    role = "Tank",
                )
            val created = createRaiderEntityResponse(id = 1L, characterName = "Testchar")
            every { raiderEntityService.create(request) } returns created

            val result = controller.create(request)

            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated raider`() {
            val request = UpdateRaiderEntityRequest(spec = "Arms", role = "DPS")
            val updated = createRaiderEntityResponse(id = 1L, spec = "Arms", role = "DPS")
            every { raiderEntityService.update(1L, request) } returns updated

            val result = controller.update(1L, request)

            result.spec shouldBe "Arms"
            result.role shouldBe "DPS"
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            every { raiderEntityService.delete(1L) } returns Unit

            val result = controller.delete(1L)

            result.statusCode shouldBe HttpStatus.NO_CONTENT
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when raider exists`() {
            every { raiderEntityService.existsById(1L) } returns true

            val result = controller.exists(1L)

            result.exists shouldBe true
        }
    }

    @Nested
    inner class FindByRealmTests {
        @Test
        fun `should return raiders for a realm`() {
            val raiders =
                listOf(
                    createRaiderEntityResponse(id = 1L, realm = "Silvermoon"),
                    createRaiderEntityResponse(id = 2L, realm = "Silvermoon"),
                )
            every { raiderEntityService.findByRealm("Silvermoon", any()) } returns PagedResponse(raiders, 0, 20, 2)

            val result = controller.findByRealm("Silvermoon", 0, null)

            result.totalElements shouldBe 2
        }
    }

    private fun createRaiderEntityResponse(
        id: Long = 1L,
        characterName: String = "Testchar",
        realm: String = "Silvermoon",
        region: String = "EU",
        wowauditId: Long? = 12345L,
        clazz: String = "Warrior",
        spec: String = "Protection",
        role: String = "Tank",
        rank: String? = "Raider",
        status: String? = "Active",
        note: String? = null,
        blizzardId: Long? = 67890L,
        trackingSince: OffsetDateTime? = OffsetDateTime.now().minusMonths(6),
        joinDate: OffsetDateTime? = OffsetDateTime.now().minusMonths(6),
        blizzardLastModified: OffsetDateTime? = OffsetDateTime.now().minusDays(1),
        lastSync: OffsetDateTime = OffsetDateTime.now(),
    ) = RaiderEntityResponse(
        id = id,
        characterName = characterName,
        realm = realm,
        region = region,
        wowauditId = wowauditId,
        clazz = clazz,
        spec = spec,
        role = role,
        rank = rank,
        status = status,
        note = note,
        blizzardId = blizzardId,
        trackingSince = trackingSince,
        joinDate = joinDate,
        blizzardLastModified = blizzardLastModified,
        lastSync = lastSync,
    )
}
