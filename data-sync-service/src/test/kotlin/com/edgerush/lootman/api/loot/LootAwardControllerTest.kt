package com.edgerush.lootman.api.loot

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
 * Unit tests for LootAwardController.
 */
class LootAwardControllerTest : UnitTest() {
    private lateinit var lootAwardService: LootAwardCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: LootAwardController

    @BeforeEach
    fun setup() {
        lootAwardService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = LootAwardController(lootAwardService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createLootAwardResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { lootAwardService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                lootAwardService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<LootAwardEntityResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { lootAwardService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return loot award when found`() {
            // Given
            val expected = createLootAwardResponse(id = 123L, itemName = "Legendary Sword")
            every { lootAwardService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.itemName shouldBe "Legendary Sword"
            verify(exactly = 1) { lootAwardService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { lootAwardService.findById(999L) } throws NoSuchElementException("Loot award not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Loot award not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created loot award with 201 status`() {
            // Given
            val request =
                CreateLootAwardEntityRequest(
                    raiderId = 1L,
                    itemId = 12345L,
                    itemName = "Legendary Sword",
                    tier = "MYTHIC",
                    flps = 0.85,
                    rdf = 0.95,
                )

            val created =
                createLootAwardResponse(
                    id = 1L,
                    raiderId = 1L,
                    itemName = "Legendary Sword",
                    tier = "MYTHIC",
                )
            every { lootAwardService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.itemName shouldBe "Legendary Sword"
            result.body?.tier shouldBe "MYTHIC"
            verify(exactly = 1) { lootAwardService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated loot award`() {
            // Given
            val request =
                UpdateLootAwardEntityRequest(
                    note = "Updated note",
                    discarded = true,
                )

            val updated =
                createLootAwardResponse(
                    id = 1L,
                    note = "Updated note",
                    discarded = true,
                )
            every { lootAwardService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.note shouldBe "Updated note"
            result.discarded shouldBe true
            verify(exactly = 1) { lootAwardService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when loot award not found`() {
            // Given
            val request = UpdateLootAwardEntityRequest(discarded = true)

            every { lootAwardService.update(999L, request) } throws NoSuchElementException("Loot award not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Loot award not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { lootAwardService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { lootAwardService.delete(1L) }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when loot award exists`() {
            // Given
            every { lootAwardService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { lootAwardService.existsById(1L) }
        }

        @Test
        fun `should return exists false when loot award does not exist`() {
            // Given
            every { lootAwardService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByRaiderTests {
        @Test
        fun `should return loot awards for a raider`() {
            // Given
            val raiderId = 1L
            val awards =
                listOf(
                    createLootAwardResponse(id = 1L, raiderId = raiderId),
                    createLootAwardResponse(id = 2L, raiderId = raiderId),
                )
            val expectedResponse =
                PagedResponse(
                    content = awards,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { lootAwardService.findByRaider(raiderId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaider(raiderId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.raiderId == raiderId } shouldBe true
        }
    }

    @Nested
    inner class FindByItemTests {
        @Test
        fun `should return loot awards for an item`() {
            // Given
            val itemId = 12345L
            val awards =
                listOf(
                    createLootAwardResponse(id = 1L, itemId = itemId),
                    createLootAwardResponse(id = 2L, itemId = itemId),
                )
            val expectedResponse =
                PagedResponse(
                    content = awards,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { lootAwardService.findByItem(itemId, any()) } returns expectedResponse

            // When
            val result = controller.findByItem(itemId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.itemId == itemId } shouldBe true
        }
    }

    @Nested
    inner class FindByTierTests {
        @Test
        fun `should return loot awards for a tier`() {
            // Given
            val tier = "MYTHIC"
            val awards =
                listOf(
                    createLootAwardResponse(id = 1L, tier = tier),
                    createLootAwardResponse(id = 2L, tier = tier),
                )
            val expectedResponse =
                PagedResponse(
                    content = awards,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { lootAwardService.findByTier(tier, any()) } returns expectedResponse

            // When
            val result = controller.findByTier(tier, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.tier == tier } shouldBe true
        }
    }

    @Nested
    inner class CountByRaiderTests {
        @Test
        fun `should return count for raider`() {
            // Given
            val raiderId = 1L
            every { lootAwardService.countByRaider(raiderId) } returns 5L

            // When
            val result = controller.countByRaider(raiderId)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { lootAwardService.countByRaider(raiderId) }
        }
    }

    private fun createLootAwardResponse(
        id: Long = 1L,
        raiderId: Long = 1L,
        itemId: Long = 12345L,
        itemName: String = "Test Item",
        tier: String = "HEROIC",
        flps: Double = 0.75,
        rdf: Double = 0.90,
        awardedAt: OffsetDateTime = OffsetDateTime.now(),
        rclootcouncilId: String? = null,
        icon: String? = null,
        slot: String? = "CHEST",
        quality: String? = "EPIC",
        responseTypeId: Int? = null,
        responseTypeName: String? = null,
        note: String? = null,
        wishValue: Int? = null,
        difficulty: String? = "HEROIC",
        discarded: Boolean? = false,
        characterId: Long? = null,
        awardedByCharacterId: Long? = null,
        awardedByName: String? = null,
    ): LootAwardEntityResponse =
        LootAwardEntityResponse(
            id = id,
            raiderId = raiderId,
            itemId = itemId,
            itemName = itemName,
            tier = tier,
            flps = flps,
            rdf = rdf,
            awardedAt = awardedAt,
            rclootcouncilId = rclootcouncilId,
            icon = icon,
            slot = slot,
            quality = quality,
            responseTypeId = responseTypeId,
            responseTypeName = responseTypeName,
            note = note,
            wishValue = wishValue,
            difficulty = difficulty,
            discarded = discarded,
            characterId = characterId,
            awardedByCharacterId = awardedByCharacterId,
            awardedByName = awardedByName,
        )
}
