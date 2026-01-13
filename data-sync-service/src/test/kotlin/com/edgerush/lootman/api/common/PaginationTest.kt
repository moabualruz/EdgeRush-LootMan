package com.edgerush.lootman.api.common

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for pagination components.
 *
 * Tests PageRequest validation and PagedResponse construction.
 */
class PaginationTest : UnitTest() {

    @Nested
    inner class PageRequestTests {

        @Test
        fun `should create PageRequest with valid page and size`() {
            val request = PageRequest(page = 0, size = 20)

            request.page shouldBe 0
            request.size shouldBe 20
            request.offset shouldBe 0
        }

        @Test
        fun `should calculate correct offset for page`() {
            val request = PageRequest(page = 2, size = 20)

            request.offset shouldBe 40
        }

        @Test
        fun `should validate page is not negative`() {
            val exception = assertThrows<IllegalArgumentException> {
                PageRequest(page = -1, size = 20)
            }

            exception.message shouldBe "Page must be non-negative"
        }

        @Test
        fun `should validate size is positive`() {
            val exception = assertThrows<IllegalArgumentException> {
                PageRequest(page = 0, size = 0)
            }

            exception.message shouldBe "Size must be positive"
        }

        @Test
        fun `should cap size at maxPageSize`() {
            val request = PageRequest(page = 0, size = 500, maxPageSize = 100)

            request.size shouldBe 100
        }

        @Test
        fun `should use default size when not specified`() {
            val request = PageRequest.withDefaults(defaultSize = 20)

            request.page shouldBe 0
            request.size shouldBe 20
        }

        @Test
        fun `should allow custom page with default size`() {
            val request = PageRequest.withDefaults(page = 5, defaultSize = 20)

            request.page shouldBe 5
            request.size shouldBe 20
            request.offset shouldBe 100
        }
    }

    @Nested
    inner class PagedResponseTests {

        @Test
        fun `should create PagedResponse with correct metadata`() {
            val items = listOf("item1", "item2", "item3")
            val response = PagedResponse(
                content = items,
                page = 0,
                size = 20,
                totalElements = 100,
            )

            response.content shouldBe items
            response.page shouldBe 0
            response.size shouldBe 20
            response.totalElements shouldBe 100
            response.totalPages shouldBe 5
            response.isFirst shouldBe true
            response.isLast shouldBe false
            response.hasNext shouldBe true
            response.hasPrevious shouldBe false
        }

        @Test
        fun `should calculate totalPages correctly`() {
            val response = PagedResponse(
                content = emptyList<String>(),
                page = 0,
                size = 20,
                totalElements = 45,
            )

            response.totalPages shouldBe 3 // 45 / 20 = 2.25, ceiling = 3
        }

        @Test
        fun `should handle empty content`() {
            val response = PagedResponse(
                content = emptyList<String>(),
                page = 0,
                size = 20,
                totalElements = 0,
            )

            response.content shouldBe emptyList()
            response.totalPages shouldBe 0
            response.isFirst shouldBe true
            response.isLast shouldBe true
            response.hasNext shouldBe false
            response.hasPrevious shouldBe false
        }

        @Test
        fun `should indicate last page correctly`() {
            val response = PagedResponse(
                content = listOf("item"),
                page = 4,
                size = 20,
                totalElements = 100,
            )

            response.isFirst shouldBe false
            response.isLast shouldBe true
            response.hasNext shouldBe false
            response.hasPrevious shouldBe true
        }

        @Test
        fun `should indicate middle page correctly`() {
            val response = PagedResponse(
                content = listOf("item1", "item2"),
                page = 2,
                size = 20,
                totalElements = 100,
            )

            response.isFirst shouldBe false
            response.isLast shouldBe false
            response.hasNext shouldBe true
            response.hasPrevious shouldBe true
        }

        @Test
        fun `should create from list with PageRequest`() {
            val allItems = (1..50).map { "item$it" }
            val pageRequest = PageRequest(page = 1, size = 20)

            val response = PagedResponse.of(
                content = allItems.drop(20).take(20),
                pageRequest = pageRequest,
                totalElements = allItems.size.toLong(),
            )

            response.content.size shouldBe 20
            response.page shouldBe 1
            response.size shouldBe 20
            response.totalElements shouldBe 50
            response.totalPages shouldBe 3
        }

        @Test
        fun `should map content to different type`() {
            val response = PagedResponse(
                content = listOf(1, 2, 3),
                page = 0,
                size = 20,
                totalElements = 3,
            )

            val mapped = response.map { it.toString() }

            mapped.content shouldBe listOf("1", "2", "3")
            mapped.page shouldBe 0
            mapped.size shouldBe 20
            mapped.totalElements shouldBe 3
        }
    }

    @Nested
    inner class PaginationPropertiesTests {

        @Test
        fun `should have default values`() {
            val properties = PaginationProperties()

            properties.defaultPageSize shouldBe 20
            properties.maxPageSize shouldBe 100
        }

        @Test
        fun `should allow custom values`() {
            val properties = PaginationProperties(
                defaultPageSize = 50,
                maxPageSize = 200,
            )

            properties.defaultPageSize shouldBe 50
            properties.maxPageSize shouldBe 200
        }

        @Test
        fun `should create PageRequest with default size`() {
            val properties = PaginationProperties(defaultPageSize = 25, maxPageSize = 100)
            val request = properties.createPageRequest(page = 2)

            request.page shouldBe 2
            request.size shouldBe 25
        }

        @Test
        fun `should create PageRequest with custom size capped at max`() {
            val properties = PaginationProperties(defaultPageSize = 20, maxPageSize = 50)
            val request = properties.createPageRequest(page = 0, size = 100)

            request.size shouldBe 50
        }
    }
}
