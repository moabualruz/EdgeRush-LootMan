package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Unit tests for JdbcWishlistRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the wishlist_items table.
 */
class JdbcWishlistRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcWishlistRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcWishlistRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should return wishlist when found`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raiderId = ?") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } returns
                listOf(
                    createWishlistItem(12345L, "Sword of Power", 1, 15.5),
                    createWishlistItem(12346L, "Shield of Defense", 2, 12.0),
                )

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            result?.raiderId shouldBe raiderId
            result?.items?.size shouldBe 2
        }

        @Test
        fun `should return null when no wishlist items found`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raiderId = ?") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<WishlistItem>>()
                val rs =
                    mockResultSet(
                        itemId = 54321L,
                        itemName = "Epic Staff",
                        priority = 1,
                        upgradePercentage = 25.5,
                        specName = "Frost",
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            val item = result?.items?.first()
            item?.itemId?.value shouldBe 54321L
            item?.itemName shouldBe "Epic Staff"
            item?.priority shouldBe 1
            item?.upgradePercentage shouldBe 25.5
            item?.specName shouldBe "Frost"
        }

        @Test
        fun `should handle null specName`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<WishlistItem>>()
                val rs =
                    mockResultSet(
                        itemId = 11111L,
                        itemName = "Generic Item",
                        priority = 1,
                        upgradePercentage = 10.0,
                        specName = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            result?.items?.first()?.specName shouldBe null
        }

        @Test
        fun `should default to Unknown Item when itemName is null`() {
            // Given - covers the elvis branch when rs.getString("itemName") returns null
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<WishlistItem>>()
                val rs =
                    mockResultSet(
                        itemId = 99999L,
                        itemName = null, // Null item name
                        priority = 1,
                        upgradePercentage = 10.0,
                        specName = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            result?.items?.first()?.itemName shouldBe "Unknown Item"
        }

        @Test
        fun `should return items sorted by priority`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("ORDER BY priority") },
                    any<RowMapper<WishlistItem>>(),
                    eq(raiderId.value),
                )
            } returns
                listOf(
                    createWishlistItem(1L, "First Item", 1, 20.0),
                    createWishlistItem(2L, "Second Item", 2, 15.0),
                    createWishlistItem(3L, "Third Item", 3, 10.0),
                )

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldNotBe null
            result?.items?.map { it.priority } shouldBe listOf(1, 2, 3)
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should delete existing items and insert new ones`() {
            // Given
            val wishlist = createWishlist()

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(wishlist)

            // Then
            result shouldBe wishlist

            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") },
                    wishlist.raiderId.value,
                )
            }
        }

        @Test
        fun `should insert all wishlist items`() {
            // Given
            val wishlist = createWishlist(itemCount = 3)

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(wishlist)

            // Then
            verify(exactly = 1) {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") },
                    wishlist.raiderId.value,
                )
            }
            verify(exactly = 3) {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should save wishlist with specName`() {
            // Given
            val item =
                WishlistItem(
                    itemId = ItemId(12345L),
                    itemName = "Spec Item",
                    priority = 1,
                    upgradePercentage = 20.0,
                    specName = "Holy",
                )
            val wishlist =
                Wishlist(
                    raiderId = RaiderId(100L),
                    items = listOf(item),
                )

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(wishlist)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    wishlist.raiderId.value,
                    item.itemId.value,
                    item.itemName,
                    item.priority,
                    item.upgradePercentage,
                    "Holy",
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete all wishlist items for raider`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(raiderId.value),
                )
            } returns 3

            // When
            repository.delete(raiderId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") && it.contains("raiderId = ?") },
                    raiderId.value,
                )
            }
        }

        @Test
        fun `should handle delete when no items exist`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(raiderId.value),
                )
            } returns 0

            // When
            repository.delete(raiderId)

            // Then - no exception should be thrown
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") },
                    raiderId.value,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        itemId: Long = 12345L,
        itemName: String? = "Test Item",
        priority: Int = 1,
        upgradePercentage: Double = 15.0,
        specName: String? = null,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("itemId") } returns itemId
        every { rs.getString("itemName") } returns itemName
        every { rs.getInt("priority") } returns priority
        every { rs.getDouble("upgradePercentage") } returns upgradePercentage
        every { rs.getString("specName") } returns specName
        return rs
    }

    private fun createWishlistItem(
        itemId: Long,
        itemName: String,
        priority: Int,
        upgradePercentage: Double,
        specName: String? = null,
    ): WishlistItem =
        WishlistItem(
            itemId = ItemId(itemId),
            itemName = itemName,
            priority = priority,
            upgradePercentage = upgradePercentage,
            specName = specName,
        )

    private fun createWishlist(
        raiderId: RaiderId = RaiderId(100L),
        itemCount: Int = 2,
    ): Wishlist {
        val items =
            (1..itemCount).map { idx ->
                createWishlistItem(
                    itemId = (12345 + idx).toLong(),
                    itemName = "Item $idx",
                    priority = idx,
                    upgradePercentage = (20.0 - idx),
                )
            }
        return Wishlist(raiderId = raiderId, items = items)
    }
}
