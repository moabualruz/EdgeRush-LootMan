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
 * Uses snake_case column names as per V0046 migration.
 *
 * Database column mappings:
 * - raider_id -> raider ID (Long)
 * - item_id -> WoW item ID (Long)
 * - item_name -> item name (String)
 * - priority -> priority rank (Int)
 * - upgrade_percentage -> upgrade value percentage (Double)
 * - spec_name -> specialization name (String?)
 */
@Repository
class JdbcWishlistRepository(
    private val jdbcTemplate: JdbcTemplate,
) : WishlistRepository {
    override fun findByRaiderId(raiderId: RaiderId): Wishlist? {
        val sql =
            """
            SELECT item_id, item_name, priority, upgrade_percentage, spec_name
            FROM wishlist_items
            WHERE raider_id = ?
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
        val deleteSql = "DELETE FROM wishlist_items WHERE raider_id = ?"
        jdbcTemplate.update(deleteSql, wishlist.raiderId.value)

        // Insert new wishlist items
        val insertSql =
            """
            INSERT INTO wishlist_items (
                raider_id, item_id, item_name, priority, upgrade_percentage, spec_name
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
        val sql = "DELETE FROM wishlist_items WHERE raider_id = ?"
        jdbcTemplate.update(sql, raiderId.value)
    }

    private val wishlistItemRowMapper =
        RowMapper { rs, _ ->
            WishlistItem(
                itemId = ItemId(rs.getLong("item_id")),
                itemName = rs.getString("item_name") ?: "Unknown Item",
                priority = rs.getInt("priority"),
                upgradePercentage = rs.getDouble("upgrade_percentage"),
                specName = rs.getString("spec_name"),
            )
        }
}
