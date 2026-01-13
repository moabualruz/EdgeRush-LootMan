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
 * Column names follow the JPA naming conventions from V0019 migration.
 *
 * Database column mappings:
 * - raiderId -> raider_id (Long)
 * - gearSet -> gear set type (String)
 * - slot -> equipment slot (String)
 * - itemId -> item_id (Long)
 * - itemLevel -> item level (Int)
 * - quality -> item quality (Int)
 * - enchant -> enchant text (String?)
 * - sockets -> socket count (Int)
 * - name -> item name (String)
 */
@Repository
class JdbcGearRepository(
    private val jdbcTemplate: JdbcTemplate
) : GearRepository {

    override fun findCurrentGear(raiderId: RaiderId): GearSet? {
        return findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)
    }

    override fun findByRaiderIdAndType(raiderId: RaiderId, gearSetType: GearSetType): GearSet? {
        val sql = """
            SELECT slot, itemId, name, itemLevel, quality, enchant, sockets
            FROM raider_gear_items
            WHERE raiderId = ? AND gearSet = ?
        """.trimIndent()

        val items = jdbcTemplate.query(sql, gearItemRowMapper, raiderId.value, gearSetType.name)

        if (items.isEmpty()) {
            return null
        }

        val itemsMap = items.associateBy { it.slot }
        return GearSet(items = itemsMap, gearSetType = gearSetType)
    }

    override fun save(raiderId: RaiderId, gearSet: GearSet): GearSet {
        // Delete existing gear items for this raider and gear set type
        val deleteSql = "DELETE FROM raider_gear_items WHERE raiderId = ? AND gearSet = ?"
        jdbcTemplate.update(deleteSql, raiderId.value, gearSet.gearSetType.name)

        // Insert new gear items
        val insertSql = """
            INSERT INTO raider_gear_items (
                raiderId, gearSet, slot, itemId, itemLevel, quality, enchant, sockets, name
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
                item.name
            )
        }

        return gearSet
    }

    private val gearItemRowMapper = RowMapper { rs, _ ->
        val slotStr = rs.getString("slot")
        val slot = EquipmentSlot.entries.firstOrNull { it.name == slotStr } ?: EquipmentSlot.HEAD

        val qualityInt = rs.getInt("quality")
        val quality = ItemQuality.fromInt(qualityInt) ?: ItemQuality.EPIC

        GearItem(
            itemId = ItemId(rs.getLong("itemId")),
            name = rs.getString("name") ?: "Unknown Item",
            itemLevel = rs.getInt("itemLevel"),
            quality = quality,
            slot = slot,
            isTierPiece = false, // Not stored in DB, would need additional logic
            enchant = rs.getString("enchant"),
            sockets = rs.getInt("sockets")
        )
    }
}
