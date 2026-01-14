package com.edgerush.lootman.api.common

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Unit tests for BaseCrudController.
 *
 * Tests the abstract controller using a concrete test implementation.
 */
class BaseCrudControllerTest : UnitTest() {

    // Test DTOs
    data class TestCreateRequest(val name: String)
    data class TestUpdateRequest(val name: String)
    data class TestResponse(val id: Long, val name: String)

    // Mock service
    private lateinit var mockService: CrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: TestCrudController

    // Concrete implementation for testing
    inner class TestCrudController(
        service: CrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>,
        paginationProperties: PaginationProperties,
    ) : BaseCrudController<Long, TestCreateRequest, TestUpdateRequest, TestResponse>(
        service,
        paginationProperties,
    )

    @BeforeEach
    fun setUp() {
        mockService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = TestCrudController(mockService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(TestResponse(1, "Test")),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { mockService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                mockService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should use custom page size when provided`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(TestResponse(1, "Test")),
                page = 0,
                size = 50,
                totalElements = 1,
            )
            every { mockService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = 50)

            // Then
            result shouldBe expectedResponse
            verify {
                mockService.findAll(match { it.page == 0 && it.size == 50 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = io.mockk.slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<TestResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { mockService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then - maxPageSize is 100, so 500 should be capped to 100
            slot.captured.size shouldBe 100
            slot.captured.maxPageSize shouldBe 100
        }

        @Test
        fun `should use provided page number`() {
            // Given
            val expectedResponse = PagedResponse(
                content = emptyList<TestResponse>(),
                page = 5,
                size = 20,
                totalElements = 100,
            )
            every { mockService.findAll(any()) } returns expectedResponse

            // When
            controller.findAll(page = 5, size = null)

            // Then
            verify {
                mockService.findAll(match { it.page == 5 })
            }
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return entity when found`() {
            // Given
            val expected = TestResponse(1, "Test Entity")
            every { mockService.findById(1L) } returns expected

            // When
            val result = controller.findById(1L)

            // Then
            result shouldBe expected
            verify { mockService.findById(1L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { mockService.findById(999L) } throws NoSuchElementException("Entity not found")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Entity not found"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created entity with 201 status`() {
            // Given
            val request = TestCreateRequest("New Entity")
            val created = TestResponse(1, "New Entity")
            every { mockService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body shouldBe created
            verify { mockService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated entity`() {
            // Given
            val request = TestUpdateRequest("Updated Name")
            val updated = TestResponse(1, "Updated Name")
            every { mockService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result shouldBe updated
            verify { mockService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when entity not found`() {
            // Given
            val request = TestUpdateRequest("Updated")
            every { mockService.update(999L, request) } throws NoSuchElementException("Entity not found")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Entity not found"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { mockService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify { mockService.delete(1L) }
        }

        @Test
        fun `should propagate exception when entity not found`() {
            // Given
            every { mockService.delete(999L) } throws NoSuchElementException("Entity not found")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Entity not found"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when entity exists`() {
            // Given
            every { mockService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify { mockService.existsById(1L) }
        }

        @Test
        fun `should return exists false when entity does not exist`() {
            // Given
            every { mockService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
            verify { mockService.existsById(999L) }
        }
    }
}

/**
 * Unit tests for ExistsResponse DTO.
 */
class ExistsResponseTest : UnitTest() {

    @Test
    fun `should create ExistsResponse with exists true`() {
        val response = ExistsResponse(exists = true)
        response.exists shouldBe true
    }

    @Test
    fun `should create ExistsResponse with exists false`() {
        val response = ExistsResponse(exists = false)
        response.exists shouldBe false
    }
}

/**
 * Unit tests for CountResponse DTO.
 */
class CountResponseTest : UnitTest() {

    @Test
    fun `should create CountResponse with count`() {
        val response = CountResponse(count = 42)
        response.count shouldBe 42
    }

    @Test
    fun `should handle zero count`() {
        val response = CountResponse(count = 0)
        response.count shouldBe 0
    }

    @Test
    fun `should handle large count`() {
        val response = CountResponse(count = Long.MAX_VALUE)
        response.count shouldBe Long.MAX_VALUE
    }
}
