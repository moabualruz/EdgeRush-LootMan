package com.edgerush.lootman.api.wishlist

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.wishlist.DeleteWishlistCommand
import com.edgerush.lootman.application.wishlist.DeleteWishlistUseCase
import com.edgerush.lootman.application.wishlist.GetWishlistQuery
import com.edgerush.lootman.application.wishlist.GetWishlistUseCase
import com.edgerush.lootman.application.wishlist.SaveWishlistCommand
import com.edgerush.lootman.application.wishlist.SaveWishlistUseCase
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
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
 * Unit tests for WishlistController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class WishlistControllerTest : UnitTest() {
    private lateinit var getWishlistUseCase: GetWishlistUseCase
    private lateinit var saveWishlistUseCase: SaveWishlistUseCase
    private lateinit var deleteWishlistUseCase: DeleteWishlistUseCase
    private lateinit var controller: WishlistController

    @BeforeEach
    fun setup() {
        getWishlistUseCase = mockk()
        saveWishlistUseCase = mockk()
        deleteWishlistUseCase = mockk()
        controller = WishlistController(
            getWishlistUseCase,
            saveWishlistUseCase,
            deleteWishlistUseCase
        )
    }

    @Nested
    inner class GetWishlistTests {
        @Test
        fun `should return wishlist when found`() {
            // Given
            val wishlist = createWishlist(raiderId = RaiderId(123L))

            every { getWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.getWishlist(123L)

            // Then
            response.raiderId shouldBe 123L
            response.items.size shouldBe 1
            response.itemCount shouldBe 1

            verify(exactly = 1) { getWishlistUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<GetWishlistQuery>()
            val wishlist = createWishlist(raiderId = RaiderId(456L))

            every { getWishlistUseCase.execute(capture(querySlot)) } returns Result.success(wishlist)

            // When
            controller.getWishlist(456L)

            // Then
            querySlot.captured.raiderId shouldBe 456L
        }

        @Test
        fun `should throw exception when wishlist not found`() {
            // Given
            every { getWishlistUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Wishlist not found for raider: 999")
            )

            // When/Then
            try {
                controller.getWishlist(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Wishlist not found for raider: 999"
            }
        }

        @Test
        fun `should return top priority item in response`() {
            // Given
            val items = listOf(
                createWishlistItem(itemId = ItemId(100L), itemName = "Low Priority", priority = 1),
                createWishlistItem(itemId = ItemId(200L), itemName = "High Priority", priority = 5),
                createWishlistItem(itemId = ItemId(300L), itemName = "Medium Priority", priority = 3)
            )
            val wishlist = createWishlist(items = items)

            every { getWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.getWishlist(1L)

            // Then
            response.topPriorityItem?.itemName shouldBe "High Priority"
            response.topPriorityItem?.priority shouldBe 5
        }
    }

    @Nested
    inner class CreateWishlistTests {
        @Test
        fun `should return CREATED status with wishlist response`() {
            // Given
            val request = SaveWishlistRequest(
                raiderId = 1L,
                items = listOf(
                    WishlistItemRequest(
                        itemId = 100L,
                        itemName = "Test Item",
                        priority = 1,
                        upgradePercentage = 15.5
                    )
                )
            )

            val wishlist = createWishlist()

            every { saveWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.createWishlist(request)

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.raiderId shouldBe 1L

            verify(exactly = 1) { saveWishlistUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request = SaveWishlistRequest(
                raiderId = 42L,
                items = listOf(
                    WishlistItemRequest(
                        itemId = 100L,
                        itemName = "Item 1",
                        priority = 1,
                        upgradePercentage = 10.0,
                        specName = "Arms"
                    ),
                    WishlistItemRequest(
                        itemId = 200L,
                        itemName = "Item 2",
                        priority = 2,
                        upgradePercentage = 20.0
                    )
                )
            )

            val commandSlot = slot<SaveWishlistCommand>()
            val wishlist = createWishlist(raiderId = RaiderId(42L))

            every { saveWishlistUseCase.execute(capture(commandSlot)) } returns Result.success(wishlist)

            // When
            controller.createWishlist(request)

            // Then
            commandSlot.captured.raiderId shouldBe 42L
            commandSlot.captured.items.size shouldBe 2
            commandSlot.captured.items[0].itemId shouldBe 100L
            commandSlot.captured.items[0].itemName shouldBe "Item 1"
            commandSlot.captured.items[0].specName shouldBe "Arms"
            commandSlot.captured.items[1].itemId shouldBe 200L
        }

        @Test
        fun `should throw exception when use case fails`() {
            // Given
            val request = SaveWishlistRequest(
                raiderId = 1L,
                items = listOf(
                    WishlistItemRequest(
                        itemId = 100L,
                        itemName = "",
                        priority = 0,
                        upgradePercentage = -1.0
                    )
                )
            )

            every { saveWishlistUseCase.execute(any()) } returns Result.failure(
                IllegalArgumentException("Priority must be positive")
            )

            // When/Then
            try {
                controller.createWishlist(request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: IllegalArgumentException) {
                e.message shouldBe "Priority must be positive"
            }
        }
    }

    @Nested
    inner class UpdateWishlistTests {
        @Test
        fun `should return updated wishlist`() {
            // Given
            val request = SaveWishlistRequest(
                raiderId = 1L,
                items = listOf(
                    WishlistItemRequest(
                        itemId = 100L,
                        itemName = "Updated Item",
                        priority = 3,
                        upgradePercentage = 25.0
                    )
                )
            )

            val updatedWishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = ItemId(100L), itemName = "Updated Item", priority = 3, upgradePercentage = 25.0)
                )
            )

            every { saveWishlistUseCase.execute(any()) } returns Result.success(updatedWishlist)

            // When
            val response = controller.updateWishlist(1L, request)

            // Then
            response.raiderId shouldBe 1L
            response.items[0].itemName shouldBe "Updated Item"
            response.items[0].priority shouldBe 3

            verify(exactly = 1) { saveWishlistUseCase.execute(any()) }
        }

        @Test
        fun `should use path variable raiderId instead of request body`() {
            // Given
            val request = SaveWishlistRequest(
                raiderId = 999L, // This should be ignored
                items = listOf(
                    WishlistItemRequest(
                        itemId = 100L,
                        itemName = "Test Item",
                        priority = 1,
                        upgradePercentage = 10.0
                    )
                )
            )

            val commandSlot = slot<SaveWishlistCommand>()
            val wishlist = createWishlist(raiderId = RaiderId(42L))

            every { saveWishlistUseCase.execute(capture(commandSlot)) } returns Result.success(wishlist)

            // When
            controller.updateWishlist(42L, request)

            // Then
            commandSlot.captured.raiderId shouldBe 42L // Path variable takes precedence
        }
    }

    @Nested
    inner class DeleteWishlistTests {
        @Test
        fun `should return NO_CONTENT on successful deletion`() {
            // Given
            every { deleteWishlistUseCase.execute(any()) } returns Result.success(Unit)

            // When
            val response = controller.deleteWishlist(1L)

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
            response.body shouldBe null

            verify(exactly = 1) { deleteWishlistUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val commandSlot = slot<DeleteWishlistCommand>()

            every { deleteWishlistUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // When
            controller.deleteWishlist(42L)

            // Then
            commandSlot.captured.raiderId shouldBe 42L
        }

        @Test
        fun `should throw exception when wishlist not found`() {
            // Given
            every { deleteWishlistUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Wishlist not found for raider: 999")
            )

            // When/Then
            try {
                controller.deleteWishlist(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Wishlist not found for raider: 999"
            }
        }
    }

    @Nested
    inner class WishlistResponseMappingTests {
        @Test
        fun `should correctly map all wishlist fields to response`() {
            // Given
            val items = listOf(
                createWishlistItem(
                    itemId = ItemId(100L),
                    itemName = "Legendary Sword",
                    priority = 1,
                    upgradePercentage = 15.5,
                    specName = "Fury"
                ),
                createWishlistItem(
                    itemId = ItemId(200L),
                    itemName = "Epic Helm",
                    priority = 2,
                    upgradePercentage = 8.2,
                    specName = "Protection"
                )
            )
            val wishlist = createWishlist(raiderId = RaiderId(123L), items = items)

            every { getWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.getWishlist(123L)

            // Then
            response.raiderId shouldBe 123L
            response.itemCount shouldBe 2
            response.items[0].itemId shouldBe 100L
            response.items[0].itemName shouldBe "Legendary Sword"
            response.items[0].priority shouldBe 1
            response.items[0].upgradePercentage shouldBe 15.5
            response.items[0].specName shouldBe "Fury"
            response.items[1].itemId shouldBe 200L
            response.items[1].specName shouldBe "Protection"
        }

        @Test
        fun `should calculate normalized upgrade value correctly`() {
            // Given
            val items = listOf(
                createWishlistItem(upgradePercentage = 50.0)
            )
            val wishlist = createWishlist(items = items)

            every { getWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.getWishlist(1L)

            // Then
            response.items[0].normalizedUpgradeValue shouldBe 0.5
        }

        @Test
        fun `should return null top priority item for empty wishlist`() {
            // Given
            val wishlist = createWishlist(items = emptyList())

            every { getWishlistUseCase.execute(any()) } returns Result.success(wishlist)

            // When
            val response = controller.getWishlist(1L)

            // Then
            response.itemCount shouldBe 0
            response.topPriorityItem shouldBe null
        }
    }

    private fun createWishlist(
        raiderId: RaiderId = RaiderId(1L),
        items: List<WishlistItem> = listOf(createWishlistItem())
    ): Wishlist = Wishlist(
        raiderId = raiderId,
        items = items
    )

    private fun createWishlistItem(
        itemId: ItemId = ItemId(12345L),
        itemName: String = "Test Item",
        priority: Int = 1,
        upgradePercentage: Double = 10.0,
        specName: String? = null
    ): WishlistItem = WishlistItem(
        itemId = itemId,
        itemName = itemName,
        priority = priority,
        upgradePercentage = upgradePercentage,
        specName = specName
    )
}
