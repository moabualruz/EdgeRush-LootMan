package com.edgerush.lootman.application.wishlist

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for Wishlist use cases.
 *
 * Tests use case business logic by mocking the repository layer.
 */
class WishlistUseCasesTest : UnitTest() {
    private lateinit var wishlistRepository: WishlistRepository

    @BeforeEach
    fun setup() {
        wishlistRepository = mockk()
    }

    @Nested
    inner class GetWishlistUseCaseTests {
        private lateinit var useCase: GetWishlistUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = GetWishlistUseCase(wishlistRepository)
        }

        @Test
        fun `should return wishlist when found`() {
            // Given
            val wishlist = createWishlist(raiderId = RaiderId(1L))
            val query = GetWishlistQuery(raiderId = 1L)

            every { wishlistRepository.findByRaiderId(RaiderId(1L)) } returns wishlist

            // When
            val result = useCase.execute(query)

            // Then
            result.isSuccess shouldBe true
            val foundWishlist = result.getOrThrow()
            foundWishlist.raiderId.value shouldBe 1L
            foundWishlist.items.size shouldBe 1
        }

        @Test
        fun `should fail when wishlist not found`() {
            // Given
            val query = GetWishlistQuery(raiderId = 999L)

            every { wishlistRepository.findByRaiderId(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Wishlist not found for raider: 999"
        }
    }

    @Nested
    inner class SaveWishlistUseCaseTests {
        private lateinit var useCase: SaveWishlistUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = SaveWishlistUseCase(wishlistRepository)
        }

        @Test
        fun `should create new wishlist`() {
            // Given
            val command =
                SaveWishlistCommand(
                    raiderId = 1L,
                    items =
                        listOf(
                            WishlistItemCommand(
                                itemId = 100L,
                                itemName = "Epic Sword",
                                priority = 1,
                                upgradePercentage = 15.5,
                                specName = "Arms",
                            ),
                            WishlistItemCommand(
                                itemId = 200L,
                                itemName = "Legendary Helm",
                                priority = 2,
                                upgradePercentage = 20.0,
                            ),
                        ),
                )

            val savedWishlistSlot = slot<Wishlist>()
            every { wishlistRepository.save(capture(savedWishlistSlot)) } answers { savedWishlistSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val savedWishlist = result.getOrThrow()
            savedWishlist.raiderId.value shouldBe 1L
            savedWishlist.items.size shouldBe 2
            savedWishlist.items[0].itemId.value shouldBe 100L
            savedWishlist.items[0].itemName shouldBe "Epic Sword"
            savedWishlist.items[0].priority shouldBe 1
            savedWishlist.items[0].upgradePercentage shouldBe 15.5
            savedWishlist.items[0].specName shouldBe "Arms"
            savedWishlist.items[1].itemId.value shouldBe 200L
            savedWishlist.items[1].specName shouldBe null

            verify(exactly = 1) { wishlistRepository.save(any()) }
        }

        @Test
        fun `should update existing wishlist`() {
            // Given
            val command =
                SaveWishlistCommand(
                    raiderId = 1L,
                    items =
                        listOf(
                            WishlistItemCommand(
                                itemId = 300L,
                                itemName = "New Item",
                                priority = 1,
                                upgradePercentage = 25.0,
                            ),
                        ),
                )

            val savedWishlistSlot = slot<Wishlist>()
            every { wishlistRepository.save(capture(savedWishlistSlot)) } answers { savedWishlistSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val savedWishlist = result.getOrThrow()
            savedWishlist.items.size shouldBe 1
            savedWishlist.items[0].itemName shouldBe "New Item"
        }

        @Test
        fun `should create wishlist with empty items`() {
            // Given
            val command =
                SaveWishlistCommand(
                    raiderId = 1L,
                    items = emptyList(),
                )

            val savedWishlistSlot = slot<Wishlist>()
            every { wishlistRepository.save(capture(savedWishlistSlot)) } answers { savedWishlistSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val savedWishlist = result.getOrThrow()
            savedWishlist.items.size shouldBe 0
        }
    }

    @Nested
    inner class DeleteWishlistUseCaseTests {
        private lateinit var useCase: DeleteWishlistUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = DeleteWishlistUseCase(wishlistRepository)
        }

        @Test
        fun `should delete existing wishlist`() {
            // Given
            val existingWishlist = createWishlist(raiderId = RaiderId(1L))
            val command = DeleteWishlistCommand(raiderId = 1L)

            every { wishlistRepository.findByRaiderId(RaiderId(1L)) } returns existingWishlist
            every { wishlistRepository.delete(RaiderId(1L)) } returns Unit

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true

            verify(exactly = 1) { wishlistRepository.delete(RaiderId(1L)) }
        }

        @Test
        fun `should fail when wishlist not found`() {
            // Given
            val command = DeleteWishlistCommand(raiderId = 999L)

            every { wishlistRepository.findByRaiderId(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Wishlist not found for raider: 999"

            verify(exactly = 0) { wishlistRepository.delete(any()) }
        }
    }

    private fun createWishlist(
        raiderId: RaiderId = RaiderId(1L),
        items: List<WishlistItem> = listOf(createWishlistItem()),
    ): Wishlist =
        Wishlist(
            raiderId = raiderId,
            items = items,
        )

    private fun createWishlistItem(
        itemId: ItemId = ItemId(12345L),
        itemName: String = "Test Item",
        priority: Int = 1,
        upgradePercentage: Double = 10.0,
        specName: String? = null,
    ): WishlistItem =
        WishlistItem(
            itemId = itemId,
            itemName = itemName,
            priority = priority,
            upgradePercentage = upgradePercentage,
            specName = specName,
        )
}
