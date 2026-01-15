package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of WishlistRepository.
 *
 * Persists Wishlist aggregates to the wishlist_items table.
 * Column names follow the JPA naming conventions from V0021 migration.
 *
 * Database column mappings:
 * - raiderId -> raider ID (Long)
 * - itemId -> WoW item ID (Long)
 * - itemName -> item name (String)
 * - priority -> priority rank (Int)
 * - upgradePercentage -> upgrade value percentage (Double)
 * - specName -> specialization name (String?)
 */
@Repository
class JdbcWishlistRepository(
    private val jdbcTemplate: JdbcTemplate,
) : WishlistRepository {
    override fun findByRaiderId(raiderId: RaiderId): Wishlist? {
        val sql =
            """
            SELECT itemId, itemName, priority, upgradePercentage, specName
            FROM wishlist_items
            WHERE raiderId = ?
            ORDER BY priority ASC
            """.trimIndent()

        val items = jdbcTemplate.query(sql, wishlistItemRowMapper, raiderId.value)

        if (items.isEmpty()) {
            return null
        }

        return Wishlist(raiderId = raiderId, items = items)
    }

    override fun save(wishlist: Wishlist): Wishlist {
        // Delete existing wishlist items for this raider
        val deleteSql = "DELETE FROM wishlist_items WHERE raiderId = ?"
        jdbcTemplate.update(deleteSql, wishlist.raiderId.value)

        // Insert new wishlist items
        val insertSql =
            """
            INSERT INTO wishlist_items (
                raiderId, itemId, itemName, priority, upgradePercentage, specName
            ) VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

        wishlist.items.forEach { item ->
            jdbcTemplate.update(
                insertSql,
                wishlist.raiderId.value,
                item.itemId.value,
                item.itemName,
                item.priority,
                item.upgradePercentage,
                item.specName,
            )
        }

        return wishlist
    }

    override fun delete(raiderId: RaiderId) {
        val sql = "DELETE FROM wishlist_items WHERE raiderId = ?"
        jdbcTemplate.update(sql, raiderId.value)
    }

    private val wishlistItemRowMapper =
        RowMapper { rs, _ ->
            WishlistItem(
                itemId = ItemId(rs.getLong("itemId")),
                itemName = rs.getString("itemName") ?: "Unknown Item",
                priority = rs.getInt("priority"),
                upgradePercentage = rs.getDouble("upgradePercentage"),
                specName = rs.getString("specName"),
            )
        }
}
