package com.edgerush.lootman.api.flps

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
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Unit tests for FlpsModifierController.
 *
 * Tests both default and guild-specific modifier endpoints.
 */
class FlpsModifierControllerTest : UnitTest() {

    private lateinit var defaultModifierService: FlpsDefaultModifierCrudService
    private lateinit var guildModifierService: FlpsGuildModifierCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: FlpsModifierController

    @BeforeEach
    fun setup() {
        defaultModifierService = mockk()
        guildModifierService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = FlpsModifierController(defaultModifierService, guildModifierService, paginationProperties)
    }

    @Nested
    inner class DefaultModifierTests {

        @Nested
        inner class FindAllDefaultsTests {

            @Test
            fun `should return paged response with default pagination`() {
                // Given
                val expectedResponse = PagedResponse(
                    content = listOf(createDefaultModifierResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
                every { defaultModifierService.findAll(any()) } returns expectedResponse

                // When
                val result = controller.findAllDefaults(page = 0, size = null)

                // Then
                result shouldBe expectedResponse
                verify {
                    defaultModifierService.findAll(match { it.page == 0 && it.size == 20 })
                }
            }

            @Test
            fun `should cap page size at max`() {
                // Given
                val slot = slot<PageRequest>()
                val expectedResponse = PagedResponse(
                    content = emptyList<FlpsDefaultModifierResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
                every { defaultModifierService.findAll(capture(slot)) } returns expectedResponse

                // When
                controller.findAllDefaults(page = 0, size = 500)

                // Then
                slot.captured.size shouldBe 100
            }
        }

        @Nested
        inner class FindDefaultByIdTests {

            @Test
            fun `should return modifier when found`() {
                // Given
                val expectedModifier = createDefaultModifierResponse(
                    id = 123L,
                    category = "rms",
                    modifierKey = "attendance_weight",
                )
                every { defaultModifierService.findById(123L) } returns expectedModifier

                // When
                val result = controller.findDefaultById(123L)

                // Then
                result.id shouldBe 123L
                result.category shouldBe "rms"
                result.modifierKey shouldBe "attendance_weight"
            }

            @Test
            fun `should propagate exception when not found`() {
                // Given
                every { defaultModifierService.findById(999L) } throws NoSuchElementException("Default modifier not found with id: 999")

                // When/Then
                try {
                    controller.findDefaultById(999L)
                    throw AssertionError("Expected exception was not thrown")
                } catch (e: NoSuchElementException) {
                    e.message shouldBe "Default modifier not found with id: 999"
                }
            }
        }

        @Nested
        inner class CreateDefaultTests {

            @Test
            fun `should return created modifier with 201 status`() {
                // Given
                val request = CreateFlpsDefaultModifierRequest(
                    category = "rms",
                    modifierKey = "attendance_weight",
                    modifierValue = BigDecimal("0.40"),
                    description = "Weight for attendance in RMS calculation",
                )

                val created = createDefaultModifierResponse(
                    id = 1L,
                    category = "rms",
                    modifierKey = "attendance_weight",
                )
                every { defaultModifierService.create(request) } returns created

                // When
                val result = controller.createDefault(request)

                // Then
                result.statusCode shouldBe HttpStatus.CREATED
                result.body?.id shouldBe 1L
                result.body?.category shouldBe "rms"
            }
        }

        @Nested
        inner class UpdateDefaultTests {

            @Test
            fun `should return updated modifier`() {
                // Given
                val request = UpdateFlpsDefaultModifierRequest(
                    modifierValue = BigDecimal("0.50"),
                )

                val updated = createDefaultModifierResponse(
                    id = 1L,
                    modifierValue = BigDecimal("0.50"),
                )
                every { defaultModifierService.update(1L, request) } returns updated

                // When
                val result = controller.updateDefault(1L, request)

                // Then
                result.id shouldBe 1L
                result.modifierValue shouldBe BigDecimal("0.50")
            }
        }

        @Nested
        inner class DeleteDefaultTests {

            @Test
            fun `should return 204 No Content on success`() {
                // Given
                every { defaultModifierService.delete(1L) } returns Unit

                // When
                val result = controller.deleteDefault(1L)

                // Then
                result.statusCode shouldBe HttpStatus.NO_CONTENT
            }
        }

        @Nested
        inner class FindDefaultsByCategoryTests {

            @Test
            fun `should return modifiers for category`() {
                // Given
                val modifiers = listOf(
                    createDefaultModifierResponse(id = 1L, category = "rms", modifierKey = "attendance_weight"),
                    createDefaultModifierResponse(id = 2L, category = "rms", modifierKey = "mechanical_weight"),
                )
                val expectedResponse = PagedResponse(
                    content = modifiers,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
                every { defaultModifierService.findByCategory("rms", any()) } returns expectedResponse

                // When
                val result = controller.findDefaultsByCategory("rms", page = 0, size = null)

                // Then
                result.totalElements shouldBe 2
                result.content.all { it.category == "rms" } shouldBe true
            }
        }
    }

    @Nested
    inner class GuildModifierTests {

        @Nested
        inner class FindAllGuildModifiersTests {

            @Test
            fun `should return paged response`() {
                // Given
                val expectedResponse = PagedResponse(
                    content = listOf(createGuildModifierResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
                every { guildModifierService.findAll(any()) } returns expectedResponse

                // When
                val result = controller.findAllGuildModifiers(page = 0, size = null)

                // Then
                result shouldBe expectedResponse
            }
        }

        @Nested
        inner class FindGuildModifierByIdTests {

            @Test
            fun `should return modifier when found`() {
                // Given
                val expectedModifier = createGuildModifierResponse(
                    id = 123L,
                    guildId = "my-guild",
                    category = "rms",
                )
                every { guildModifierService.findById(123L) } returns expectedModifier

                // When
                val result = controller.findGuildModifierById(123L)

                // Then
                result.id shouldBe 123L
                result.guildId shouldBe "my-guild"
            }
        }

        @Nested
        inner class CreateGuildModifierTests {

            @Test
            fun `should return created modifier with 201 status`() {
                // Given
                val request = CreateFlpsGuildModifierRequest(
                    guildId = "my-guild",
                    category = "rms",
                    modifierKey = "attendance_weight",
                    modifierValue = BigDecimal("0.45"),
                    description = "Custom attendance weight for guild",
                )

                val created = createGuildModifierResponse(
                    id = 1L,
                    guildId = "my-guild",
                    category = "rms",
                )
                every { guildModifierService.create(request) } returns created

                // When
                val result = controller.createGuildModifier(request)

                // Then
                result.statusCode shouldBe HttpStatus.CREATED
                result.body?.guildId shouldBe "my-guild"
            }
        }

        @Nested
        inner class UpdateGuildModifierTests {

            @Test
            fun `should return updated modifier`() {
                // Given
                val request = UpdateFlpsGuildModifierRequest(
                    modifierValue = BigDecimal("0.55"),
                )

                val updated = createGuildModifierResponse(
                    id = 1L,
                    modifierValue = BigDecimal("0.55"),
                )
                every { guildModifierService.update(1L, request) } returns updated

                // When
                val result = controller.updateGuildModifier(1L, request)

                // Then
                result.modifierValue shouldBe BigDecimal("0.55")
            }
        }

        @Nested
        inner class DeleteGuildModifierTests {

            @Test
            fun `should return 204 No Content on success`() {
                // Given
                every { guildModifierService.delete(1L) } returns Unit

                // When
                val result = controller.deleteGuildModifier(1L)

                // Then
                result.statusCode shouldBe HttpStatus.NO_CONTENT
            }
        }

        @Nested
        inner class FindByGuildTests {

            @Test
            fun `should return modifiers for guild`() {
                // Given
                val guildId = "my-guild"
                val modifiers = listOf(
                    createGuildModifierResponse(id = 1L, guildId = guildId),
                    createGuildModifierResponse(id = 2L, guildId = guildId),
                )
                val expectedResponse = PagedResponse(
                    content = modifiers,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
                every { guildModifierService.findByGuild(guildId, any()) } returns expectedResponse

                // When
                val result = controller.findByGuild(guildId, page = 0, size = null)

                // Then
                result.totalElements shouldBe 2
                result.content.all { it.guildId == guildId } shouldBe true
            }
        }

        @Nested
        inner class FindByGuildAndCategoryTests {

            @Test
            fun `should return modifiers for guild and category`() {
                // Given
                val guildId = "my-guild"
                val category = "rms"
                val modifiers = listOf(
                    createGuildModifierResponse(id = 1L, guildId = guildId, category = category),
                )
                val expectedResponse = PagedResponse(
                    content = modifiers,
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
                every { guildModifierService.findByGuildAndCategory(guildId, category, any()) } returns expectedResponse

                // When
                val result = controller.findByGuildAndCategory(guildId, category, page = 0, size = null)

                // Then
                result.totalElements shouldBe 1
                result.content.all { it.guildId == guildId && it.category == category } shouldBe true
            }
        }
    }

    private fun createDefaultModifierResponse(
        id: Long = 1L,
        category: String = "rms",
        modifierKey: String = "attendance_weight",
        modifierValue: BigDecimal = BigDecimal("0.40"),
        description: String? = "Weight for attendance",
        createdAt: OffsetDateTime = OffsetDateTime.now(),
        updatedAt: OffsetDateTime = OffsetDateTime.now(),
    ): FlpsDefaultModifierResponse = FlpsDefaultModifierResponse(
        id = id,
        category = category,
        modifierKey = modifierKey,
        modifierValue = modifierValue,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun createGuildModifierResponse(
        id: Long = 1L,
        guildId: String = "test-guild",
        category: String = "rms",
        modifierKey: String = "attendance_weight",
        modifierValue: BigDecimal = BigDecimal("0.40"),
        description: String? = "Custom weight",
        createdAt: OffsetDateTime = OffsetDateTime.now(),
        updatedAt: OffsetDateTime = OffsetDateTime.now(),
    ): FlpsGuildModifierResponse = FlpsGuildModifierResponse(
        id = id,
        guildId = guildId,
        category = category,
        modifierKey = modifierKey,
        modifierValue = modifierValue,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
