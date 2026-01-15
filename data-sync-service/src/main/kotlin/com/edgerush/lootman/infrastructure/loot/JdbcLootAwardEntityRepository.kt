package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardEntityRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of LootAwardEntityRepository.
 *
 * Persists LootAwardEntity to the loot_awards table.
 */
@Repository
class JdbcLootAwardEntityRepository(
    private val jdbcTemplate: JdbcTemplate,
) : LootAwardEntityRepository {
    override fun findById(id: Long): LootAwardEntity? {
        val sql =
            """
            SELECT id, raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                   rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                   response_type_rgba, response_type_excluded, propagated_response_type_id,
                   propagated_response_type_name, propagated_response_type_rgba,
                   propagated_response_type_excluded, same_response_amount, note, wish_value,
                   difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            FROM loot_awards
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, lootAwardRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM loot_awards WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity> {
        val sql =
            """
            SELECT id, raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                   rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                   response_type_rgba, response_type_excluded, propagated_response_type_id,
                   propagated_response_type_name, propagated_response_type_rgba,
                   propagated_response_type_excluded, same_response_amount, note, wish_value,
                   difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            FROM loot_awards
            ORDER BY awarded_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM loot_awards"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity> {
        val sql =
            """
            SELECT id, raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                   rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                   response_type_rgba, response_type_excluded, propagated_response_type_id,
                   propagated_response_type_name, propagated_response_type_rgba,
                   propagated_response_type_excluded, same_response_amount, note, wish_value,
                   difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            FROM loot_awards
            WHERE raider_id = ?
            ORDER BY awarded_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, raiderId, limit, offset)
    }

    override fun countByRaiderId(raiderId: Long): Long {
        val sql = "SELECT COUNT(*) FROM loot_awards WHERE raider_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raiderId) ?: 0L
    }

    override fun findByItemId(
        itemId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity> {
        val sql =
            """
            SELECT id, raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                   rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                   response_type_rgba, response_type_excluded, propagated_response_type_id,
                   propagated_response_type_name, propagated_response_type_rgba,
                   propagated_response_type_excluded, same_response_amount, note, wish_value,
                   difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            FROM loot_awards
            WHERE item_id = ?
            ORDER BY awarded_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, itemId, limit, offset)
    }

    override fun countByItemId(itemId: Long): Long {
        val sql = "SELECT COUNT(*) FROM loot_awards WHERE item_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, itemId) ?: 0L
    }

    override fun findByTier(
        tier: String,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity> {
        val sql =
            """
            SELECT id, raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                   rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                   response_type_rgba, response_type_excluded, propagated_response_type_id,
                   propagated_response_type_name, propagated_response_type_rgba,
                   propagated_response_type_excluded, same_response_amount, note, wish_value,
                   difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            FROM loot_awards
            WHERE tier = ?
            ORDER BY awarded_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, tier, limit, offset)
    }

    override fun countByTier(tier: String): Long {
        val sql = "SELECT COUNT(*) FROM loot_awards WHERE tier = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, tier) ?: 0L
    }

    override fun save(entity: LootAwardEntity): LootAwardEntity {
        return if (entity.id == null) {
            insertLootAward(entity)
        } else {
            updateLootAward(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM loot_awards WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertLootAward(entity: LootAwardEntity): LootAwardEntity {
        val sql =
            """
            INSERT INTO loot_awards (
                raider_id, item_id, item_name, tier, flps, rdf, awarded_at,
                rclootcouncil_id, icon, slot, quality, response_type_id, response_type_name,
                response_type_rgba, response_type_excluded, propagated_response_type_id,
                propagated_response_type_name, propagated_response_type_rgba,
                propagated_response_type_excluded, same_response_amount, note, wish_value,
                difficulty, discarded, character_id, awarded_by_character_id, awarded_by_name
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, entity.raiderId)
            ps.setLong(2, entity.itemId)
            ps.setString(3, entity.itemName)
            ps.setString(4, entity.tier)
            ps.setDouble(5, entity.flps)
            ps.setDouble(6, entity.rdf)
            ps.setTimestamp(7, Timestamp.from(entity.awardedAt.toInstant()))
            entity.rclootcouncilId?.let { ps.setString(8, it) } ?: ps.setNull(8, java.sql.Types.VARCHAR)
            entity.icon?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            entity.slot?.let { ps.setString(10, it) } ?: ps.setNull(10, java.sql.Types.VARCHAR)
            entity.quality?.let { ps.setString(11, it) } ?: ps.setNull(11, java.sql.Types.VARCHAR)
            entity.responseTypeId?.let { ps.setInt(12, it) } ?: ps.setNull(12, java.sql.Types.INTEGER)
            entity.responseTypeName?.let { ps.setString(13, it) } ?: ps.setNull(13, java.sql.Types.VARCHAR)
            entity.responseTypeRgba?.let { ps.setString(14, it) } ?: ps.setNull(14, java.sql.Types.VARCHAR)
            entity.responseTypeExcluded?.let { ps.setBoolean(15, it) } ?: ps.setNull(15, java.sql.Types.BOOLEAN)
            entity.propagatedResponseTypeId?.let { ps.setInt(16, it) } ?: ps.setNull(16, java.sql.Types.INTEGER)
            entity.propagatedResponseTypeName?.let { ps.setString(17, it) } ?: ps.setNull(17, java.sql.Types.VARCHAR)
            entity.propagatedResponseTypeRgba?.let { ps.setString(18, it) } ?: ps.setNull(18, java.sql.Types.VARCHAR)
            entity.propagatedResponseTypeExcluded?.let { ps.setBoolean(19, it) } ?: ps.setNull(19, java.sql.Types.BOOLEAN)
            entity.sameResponseAmount?.let { ps.setInt(20, it) } ?: ps.setNull(20, java.sql.Types.INTEGER)
            entity.note?.let { ps.setString(21, it) } ?: ps.setNull(21, java.sql.Types.VARCHAR)
            entity.wishValue?.let { ps.setInt(22, it) } ?: ps.setNull(22, java.sql.Types.INTEGER)
            entity.difficulty?.let { ps.setString(23, it) } ?: ps.setNull(23, java.sql.Types.VARCHAR)
            entity.discarded?.let { ps.setBoolean(24, it) } ?: ps.setNull(24, java.sql.Types.BOOLEAN)
            entity.characterId?.let { ps.setLong(25, it) } ?: ps.setNull(25, java.sql.Types.BIGINT)
            entity.awardedByCharacterId?.let { ps.setLong(26, it) } ?: ps.setNull(26, java.sql.Types.BIGINT)
            entity.awardedByName?.let { ps.setString(27, it) } ?: ps.setNull(27, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateLootAward(entity: LootAwardEntity) {
        val sql =
            """
            UPDATE loot_awards SET
                raider_id = ?, item_id = ?, item_name = ?, tier = ?, flps = ?, rdf = ?,
                awarded_at = ?, rclootcouncil_id = ?, icon = ?, slot = ?, quality = ?,
                response_type_id = ?, response_type_name = ?, response_type_rgba = ?,
                response_type_excluded = ?, propagated_response_type_id = ?,
                propagated_response_type_name = ?, propagated_response_type_rgba = ?,
                propagated_response_type_excluded = ?, same_response_amount = ?, note = ?,
                wish_value = ?, difficulty = ?, discarded = ?, character_id = ?,
                awarded_by_character_id = ?, awarded_by_name = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.raiderId,
            entity.itemId,
            entity.itemName,
            entity.tier,
            entity.flps,
            entity.rdf,
            Timestamp.from(entity.awardedAt.toInstant()),
            entity.rclootcouncilId,
            entity.icon,
            entity.slot,
            entity.quality,
            entity.responseTypeId,
            entity.responseTypeName,
            entity.responseTypeRgba,
            entity.responseTypeExcluded,
            entity.propagatedResponseTypeId,
            entity.propagatedResponseTypeName,
            entity.propagatedResponseTypeRgba,
            entity.propagatedResponseTypeExcluded,
            entity.sameResponseAmount,
            entity.note,
            entity.wishValue,
            entity.difficulty,
            entity.discarded,
            entity.characterId,
            entity.awardedByCharacterId,
            entity.awardedByName,
            entity.id,
        )
    }

    private val lootAwardRowMapper =
        RowMapper { rs, _ ->
            val responseTypeIdValue = rs.getInt("response_type_id")
            val responseTypeId = if (rs.wasNull()) null else responseTypeIdValue

            val responseTypeExcludedValue = rs.getBoolean("response_type_excluded")
            val responseTypeExcluded = if (rs.wasNull()) null else responseTypeExcludedValue

            val propagatedResponseTypeIdValue = rs.getInt("propagated_response_type_id")
            val propagatedResponseTypeId = if (rs.wasNull()) null else propagatedResponseTypeIdValue

            val propagatedResponseTypeExcludedValue = rs.getBoolean("propagated_response_type_excluded")
            val propagatedResponseTypeExcluded = if (rs.wasNull()) null else propagatedResponseTypeExcludedValue

            val sameResponseAmountValue = rs.getInt("same_response_amount")
            val sameResponseAmount = if (rs.wasNull()) null else sameResponseAmountValue

            val wishValueValue = rs.getInt("wish_value")
            val wishValue = if (rs.wasNull()) null else wishValueValue

            val discardedValue = rs.getBoolean("discarded")
            val discarded = if (rs.wasNull()) null else discardedValue

            val characterIdValue = rs.getLong("character_id")
            val characterId = if (rs.wasNull()) null else characterIdValue

            val awardedByCharacterIdValue = rs.getLong("awarded_by_character_id")
            val awardedByCharacterId = if (rs.wasNull()) null else awardedByCharacterIdValue

            LootAwardEntity(
                id = rs.getLong("id"),
                raiderId = rs.getLong("raider_id"),
                itemId = rs.getLong("item_id"),
                itemName = rs.getString("item_name"),
                tier = rs.getString("tier"),
                flps = rs.getDouble("flps"),
                rdf = rs.getDouble("rdf"),
                awardedAt = rs.getTimestamp("awarded_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
                rclootcouncilId = rs.getString("rclootcouncil_id"),
                icon = rs.getString("icon"),
                slot = rs.getString("slot"),
                quality = rs.getString("quality"),
                responseTypeId = responseTypeId,
                responseTypeName = rs.getString("response_type_name"),
                responseTypeRgba = rs.getString("response_type_rgba"),
                responseTypeExcluded = responseTypeExcluded,
                propagatedResponseTypeId = propagatedResponseTypeId,
                propagatedResponseTypeName = rs.getString("propagated_response_type_name"),
                propagatedResponseTypeRgba = rs.getString("propagated_response_type_rgba"),
                propagatedResponseTypeExcluded = propagatedResponseTypeExcluded,
                sameResponseAmount = sameResponseAmount,
                note = rs.getString("note"),
                wishValue = wishValue,
                difficulty = rs.getString("difficulty"),
                discarded = discarded,
                characterId = characterId,
                awardedByCharacterId = awardedByCharacterId,
                awardedByName = rs.getString("awarded_by_name"),
            )
        }
}
