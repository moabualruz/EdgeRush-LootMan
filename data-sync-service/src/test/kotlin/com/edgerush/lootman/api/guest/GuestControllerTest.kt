package com.edgerush.lootman.api.guest

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

class GuestControllerTest : UnitTest() {

    private lateinit var guestService: GuestCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: GuestController

    @BeforeEach
    fun setup() {
        guestService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = GuestController(guestService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            val expected = PagedResponse(listOf(createGuestResponse(guestId = 1L)), 0, 20, 1)
            every { guestService.findAll(any()) } returns expected
            controller.findAll(0, null) shouldBe expected
        }

        @Test
        fun `should cap page size at max`() {
            val slot = slot<PageRequest>()
            every { guestService.findAll(capture(slot)) } returns PagedResponse(emptyList(), 0, 100, 0)
            controller.findAll(0, 500)
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return guest when found`() {
            val expected = createGuestResponse(guestId = 123L, name = "Guestchar")
            every { guestService.findById(123L) } returns expected
            controller.findById(123L).name shouldBe "Guestchar"
        }

        @Test
        fun `should propagate exception when not found`() {
            every { guestService.findById(999L) } throws NoSuchElementException("Guest not found")
            try { controller.findById(999L); throw AssertionError() } catch (e: NoSuchElementException) { }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created guest with 201 status`() {
            val request = CreateGuestRequest(guestId = 1L, name = "Guestchar")
            val created = createGuestResponse(guestId = 1L)
            every { guestService.create(request) } returns created
            val result = controller.create(request)
            result.statusCode shouldBe HttpStatus.CREATED
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            every { guestService.delete(1L) } returns Unit
            controller.delete(1L).statusCode shouldBe HttpStatus.NO_CONTENT
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when guest exists`() {
            every { guestService.existsById(1L) } returns true
            controller.exists(1L).exists shouldBe true
        }
    }

    private fun createGuestResponse(
        guestId: Long = 1L, name: String = "Guestchar", realm: String? = "Silvermoon",
        clazz: String? = "Mage", role: String? = "DPS", blizzardId: Long? = 12345L,
        trackingSince: OffsetDateTime? = OffsetDateTime.now(), syncedAt: OffsetDateTime = OffsetDateTime.now()
    ) = GuestResponse(guestId, name, realm, clazz, role, blizzardId, trackingSince, syncedAt)
}
