package com.edgerush.lootman.api.gear

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.gear.GetCurrentGearQuery
import com.edgerush.lootman.application.gear.GetCurrentGearUseCase
import com.edgerush.lootman.application.gear.GetGearByTypeQuery
import com.edgerush.lootman.application.gear.GetGearByTypeUseCase
import com.edgerush.lootman.application.gear.SaveGearCommand
import com.edgerush.lootman.application.gear.SaveGearUseCase
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Unit tests for GearController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class GearControllerTest : UnitTest() {
    private lateinit var getCurrentGearUseCase: GetCurrentGearUseCase
    private lateinit var getGearByTypeUseCase: GetGearByTypeUseCase
    private lateinit var saveGearUseCase: SaveGearUseCase
    private lateinit var controller: GearController

    @BeforeEach
    fun setup() {
        getCurrentGearUseCase = mockk()
        getGearByTypeUseCase = mockk()
        saveGearUseCase = mockk()
        controller = GearController(
            getCurrentGearUseCase,
            getGearByTypeUseCase,
            saveGearUseCase
        )
    }

    @Nested
    inner class GetCurrentGearTests {
        @Test
        fun `should return current gear when found`() {
            // Given
            val gearSet = createGearSet()

            every { getCurrentGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getCurrentGear(123L)

            // Then
            response.gearSetType shouldBe "EQUIPPED"
            response.items.size shouldBe 1
            response.averageItemLevel shouldBe 600.0

            verify(exactly = 1) { getCurrentGearUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<GetCurrentGearQuery>()
            val gearSet = createGearSet()

            every { getCurrentGearUseCase.execute(capture(querySlot)) } returns Result.success(gearSet)

            // When
            controller.getCurrentGear(456L)

            // Then
            querySlot.captured.raiderId shouldBe 456L
        }

        @Test
        fun `should throw exception when gear not found`() {
            // Given
            every { getCurrentGearUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Gear not found for raider: 999")
            )

            // When/Then
            try {
                controller.getCurrentGear(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Gear not found for raider: 999"
            }
        }
    }

    @Nested
    inner class GetGearByTypeTests {
        @Test
        fun `should return gear by type when found`() {
            // Given
            val gearSet = createGearSet(gearSetType = GearSetType.BEST)

            every { getGearByTypeUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getGearByType(123L, "BEST")

            // Then
            response.gearSetType shouldBe "BEST"

            verify(exactly = 1) { getGearByTypeUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<GetGearByTypeQuery>()
            val gearSet = createGearSet()

            every { getGearByTypeUseCase.execute(capture(querySlot)) } returns Result.success(gearSet)

            // When
            controller.getGearByType(456L, "EQUIPPED")

            // Then
            querySlot.captured.raiderId shouldBe 456L
            querySlot.captured.gearSetType shouldBe "EQUIPPED"
        }

        @Test
        fun `should throw exception when gear type not found`() {
            // Given
            every { getGearByTypeUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Gear of type BEST not found for raider: 999")
            )

            // When/Then
            try {
                controller.getGearByType(999L, "BEST")
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Gear of type BEST not found for raider: 999"
            }
        }
    }

    @Nested
    inner class CreateGearTests {
        @Test
        fun `should return CREATED status with gear response`() {
            // Given
            val request = SaveGearRequest(
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemRequest(
                        itemId = 100L,
                        name = "Test Helm",
                        itemLevel = 600,
                        quality = "EPIC",
                        slot = "HEAD"
                    )
                )
            )

            val gearSet = createGearSet()

            every { saveGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.createGear(123L, request)

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.gearSetType shouldBe "EQUIPPED"

            verify(exactly = 1) { saveGearUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request = SaveGearRequest(
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemRequest(
                        itemId = 100L,
                        name = "Test Helm",
                        itemLevel = 600,
                        quality = "EPIC",
                        slot = "HEAD",
                        isTierPiece = true,
                        enchant = "+10 Stats",
                        sockets = 2
                    )
                )
            )

            val commandSlot = slot<SaveGearCommand>()
            val gearSet = createGearSet()

            every { saveGearUseCase.execute(capture(commandSlot)) } returns Result.success(gearSet)

            // When
            controller.createGear(42L, request)

            // Then
            commandSlot.captured.raiderId shouldBe 42L
            commandSlot.captured.gearSetType shouldBe "EQUIPPED"
            commandSlot.captured.items.size shouldBe 1
            commandSlot.captured.items[0].itemId shouldBe 100L
            commandSlot.captured.items[0].name shouldBe "Test Helm"
            commandSlot.captured.items[0].isTierPiece shouldBe true
            commandSlot.captured.items[0].enchant shouldBe "+10 Stats"
            commandSlot.captured.items[0].sockets shouldBe 2
        }

        @Test
        fun `should throw exception when use case fails`() {
            // Given
            val request = SaveGearRequest(
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemRequest(
                        itemId = 100L,
                        name = "Test Item",
                        itemLevel = 0, // Invalid
                        quality = "EPIC",
                        slot = "HEAD"
                    )
                )
            )

            every { saveGearUseCase.execute(any()) } returns Result.failure(
                IllegalArgumentException("Item level must be positive")
            )

            // When/Then
            try {
                controller.createGear(1L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: IllegalArgumentException) {
                e.message shouldBe "Item level must be positive"
            }
        }
    }

    @Nested
    inner class UpdateGearTests {
        @Test
        fun `should return updated gear`() {
            // Given
            val request = SaveGearRequest(
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemRequest(
                        itemId = 100L,
                        name = "Upgraded Helm",
                        itemLevel = 620,
                        quality = "EPIC",
                        slot = "HEAD"
                    )
                )
            )

            val updatedGearSet = createGearSet(
                items = mapOf(
                    EquipmentSlot.HEAD to createGearItem(itemLevel = 620, name = "Upgraded Helm")
                )
            )

            every { saveGearUseCase.execute(any()) } returns Result.success(updatedGearSet)

            // When
            val response = controller.updateGear(1L, request)

            // Then
            response.averageItemLevel shouldBe 620.0
            response.items[0].name shouldBe "Upgraded Helm"

            verify(exactly = 1) { saveGearUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command with path variable raiderId`() {
            // Given
            val request = SaveGearRequest(
                gearSetType = "BEST",
                items = emptyList()
            )

            val commandSlot = slot<SaveGearCommand>()
            val gearSet = createGearSet(gearSetType = GearSetType.BEST, items = emptyMap())

            every { saveGearUseCase.execute(capture(commandSlot)) } returns Result.success(gearSet)

            // When
            controller.updateGear(42L, request)

            // Then
            commandSlot.captured.raiderId shouldBe 42L
            commandSlot.captured.gearSetType shouldBe "BEST"
        }
    }

    @Nested
    inner class GearSetResponseMappingTests {
        @Test
        fun `should correctly map all gear fields to response`() {
            // Given
            val items = mapOf(
                EquipmentSlot.HEAD to createGearItem(
                    itemId = ItemId(100L),
                    name = "Tier Helm",
                    itemLevel = 610,
                    quality = ItemQuality.EPIC,
                    slot = EquipmentSlot.HEAD,
                    isTierPiece = true,
                    enchant = "+10 Int",
                    sockets = 1
                ),
                EquipmentSlot.SHOULDER to createGearItem(
                    itemId = ItemId(200L),
                    name = "Tier Shoulders",
                    itemLevel = 610,
                    slot = EquipmentSlot.SHOULDER,
                    isTierPiece = true
                )
            )
            val gearSet = createGearSet(items = items)

            every { getCurrentGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getCurrentGear(123L)

            // Then
            response.gearSetType shouldBe "EQUIPPED"
            response.totalSlots shouldBe 2
            response.tierPieceCount shouldBe 2
            response.has2PieceBonus shouldBe true
            response.has4PieceBonus shouldBe false
            response.averageItemLevel shouldBe 610.0
        }

        @Test
        fun `should correctly map gear item details`() {
            // Given
            val items = mapOf(
                EquipmentSlot.CHEST to createGearItem(
                    itemId = ItemId(300L),
                    name = "Legendary Chestplate",
                    itemLevel = 639,
                    quality = ItemQuality.LEGENDARY,
                    slot = EquipmentSlot.CHEST,
                    isTierPiece = false,
                    enchant = "+15 Stats",
                    sockets = 3
                )
            )
            val gearSet = createGearSet(items = items)

            every { getCurrentGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getCurrentGear(1L)

            // Then
            val itemResponse = response.items[0]
            itemResponse.itemId shouldBe 300L
            itemResponse.name shouldBe "Legendary Chestplate"
            itemResponse.itemLevel shouldBe 639
            itemResponse.quality shouldBe "LEGENDARY"
            itemResponse.slot shouldBe "CHEST"
            itemResponse.isTierPiece shouldBe false
            itemResponse.enchant shouldBe "+15 Stats"
            itemResponse.sockets shouldBe 3
        }

        @Test
        fun `should return 4-piece bonus when 4 or more tier pieces equipped`() {
            // Given
            val items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = true),
                EquipmentSlot.HANDS to createGearItem(slot = EquipmentSlot.HANDS, isTierPiece = true)
            )
            val gearSet = createGearSet(items = items)

            every { getCurrentGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getCurrentGear(1L)

            // Then
            response.tierPieceCount shouldBe 4
            response.has2PieceBonus shouldBe true
            response.has4PieceBonus shouldBe true
        }

        @Test
        fun `should handle empty gear set`() {
            // Given
            val gearSet = createGearSet(items = emptyMap())

            every { getCurrentGearUseCase.execute(any()) } returns Result.success(gearSet)

            // When
            val response = controller.getCurrentGear(1L)

            // Then
            response.items.size shouldBe 0
            response.totalSlots shouldBe 0
            response.averageItemLevel shouldBe 0.0
            response.tierPieceCount shouldBe 0
            response.has2PieceBonus shouldBe false
            response.has4PieceBonus shouldBe false
        }
    }

    private fun createGearSet(
        items: Map<EquipmentSlot, GearItem> = mapOf(
            EquipmentSlot.HEAD to createGearItem()
        ),
        gearSetType: GearSetType = GearSetType.EQUIPPED
    ): GearSet = GearSet(
        items = items,
        gearSetType = gearSetType
    )

    private fun createGearItem(
        itemId: ItemId = ItemId(12345L),
        name: String = "Test Item",
        itemLevel: Int = 600,
        quality: ItemQuality = ItemQuality.EPIC,
        slot: EquipmentSlot = EquipmentSlot.HEAD,
        isTierPiece: Boolean = false,
        enchant: String? = null,
        sockets: Int = 0
    ): GearItem = GearItem(
        itemId = itemId,
        name = name,
        itemLevel = itemLevel,
        quality = quality,
        slot = slot,
        isTierPiece = isTierPiece,
        enchant = enchant,
        sockets = sockets
    )
}
