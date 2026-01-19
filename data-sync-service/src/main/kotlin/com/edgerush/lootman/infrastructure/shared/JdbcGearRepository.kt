package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import com.edgerush.lootman.domain.shared.repository.GearRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of GearRepository.
 *
 * Persists GearSet aggregates to the raider_gear_items table.
 * Uses snake_case column names as per V0045 migration.
 *
 * Database column mappings:
 * - raider_id -> raider ID (Long)
 * - gear_set -> gear set type (String)
 * - slot -> equipment slot (String)
 * - item_id -> item ID (Long)
 * - item_level -> item level (Int)
 * - quality -> item quality (Int)
 * - enchant -> enchant text (String?)
 * - sockets -> socket count (Int)
 * - name -> item name (String)
 */
@Repository
class JdbcGearRepository(
    private val jdbcTemplate: JdbcTemplate,
) : GearRepository {
    override fun findCurrentGear(raiderId: RaiderId): GearSet? {
        return findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)
    }

    override fun findByRaiderIdAndType(
        raiderId: RaiderId,
        gearSetType: GearSetType,
    ): GearSet? {
        val sql =
            """
            SELECT slot, item_id, name, item_level, quality, enchant, sockets
            FROM raider_gear_items
            WHERE raider_id = ? AND gear_set = ?
            """.trimIndent()

        val items = jdbcTemplate.query(sql, gearItemRowMapper, raiderId.value, gearSetType.name)

        if (items.isEmpty()) {
            return null
        }

        val itemsMap = items.associateBy { it.slot }
        return GearSet(items = itemsMap, gearSetType = gearSetType)
    }

    override fun save(
        raiderId: RaiderId,
        gearSet: GearSet,
    ): GearSet {
        // Delete existing gear items for this raider and gear set type
        val deleteSql = "DELETE FROM raider_gear_items WHERE raider_id = ? AND gear_set = ?"
        jdbcTemplate.update(deleteSql, raiderId.value, gearSet.gearSetType.name)

        // Insert new gear items
        val insertSql =
            """
            INSERT INTO raider_gear_items (
                raider_id, gear_set, slot, item_id, item_level, quality, enchant, sockets, name
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        gearSet.items.forEach { (slot, item) ->
            jdbcTemplate.update(
                insertSql,
                raiderId.value,
                gearSet.gearSetType.name,
                slot.name,
                item.itemId.value,
                item.itemLevel,
                item.quality.ordinal,
                item.enchant,
                item.sockets,
                item.name,
            )
        }

        return gearSet
    }

    private val gearItemRowMapper =
        RowMapper { rs, _ ->
            val slotStr = rs.getString("slot")
            val slot = EquipmentSlot.entries.firstOrNull { it.name == slotStr } ?: EquipmentSlot.HEAD

            val qualityInt = rs.getInt("quality")
            val quality = ItemQuality.fromInt(qualityInt) ?: ItemQuality.EPIC

            GearItem(
                itemId = ItemId(rs.getLong("item_id")),
                name = rs.getString("name") ?: "Unknown Item",
                itemLevel = rs.getInt("item_level"),
                quality = quality,
                slot = slot,
                isTierPiece = false, // Not stored in DB, would need additional logic
                enchant = rs.getString("enchant"),
                sockets = rs.getInt("sockets"),
            )
        }
}
