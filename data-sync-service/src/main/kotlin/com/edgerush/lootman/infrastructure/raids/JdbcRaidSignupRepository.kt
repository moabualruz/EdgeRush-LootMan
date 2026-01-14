package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.lootman.domain.raids.repository.RaidSignupRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

/**
 * JDBC implementation of RaidSignupRepository.
 *
 * Persists RaidSignup entities to the raid_signups table.
 */
@Repository
class JdbcRaidSignupRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaidSignupRepository {

    override fun findById(id: Long): RaidSignupEntity? {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm,
                   character_region, character_class, character_role, character_guest,
                   status, comment, selected
            FROM raid_signups
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, signupRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM raid_signups WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<RaidSignupEntity> {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm,
                   character_region, character_class, character_role, character_guest,
                   status, comment, selected
            FROM raid_signups
            ORDER BY raid_id, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, signupRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM raid_signups"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm,
                   character_region, character_class, character_role, character_guest,
                   status, comment, selected
            FROM raid_signups
            WHERE raid_id = ?
            ORDER BY character_name, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, signupRowMapper, raidId, limit, offset)
    }

    override fun countByRaidId(raidId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raid_signups WHERE raid_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raidId) ?: 0L
    }

    override fun findSelectedByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm,
                   character_region, character_class, character_role, character_guest,
                   status, comment, selected
            FROM raid_signups
            WHERE raid_id = ? AND selected = true
            ORDER BY character_name, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, signupRowMapper, raidId, limit, offset)
    }

    override fun countSelectedByRaidId(raidId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raid_signups WHERE raid_id = ? AND selected = true"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raidId) ?: 0L
    }

    override fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm,
                   character_region, character_class, character_role, character_guest,
                   status, comment, selected
            FROM raid_signups
            WHERE character_id = ?
            ORDER BY raid_id DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, signupRowMapper, characterId, limit, offset)
    }

    override fun countByCharacterId(characterId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raid_signups WHERE character_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, characterId) ?: 0L
    }

    override fun save(signup: RaidSignupEntity): RaidSignupEntity {
        return if (signup.id == null) {
            insertSignup(signup)
        } else {
            updateSignup(signup)
            signup
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM raid_signups WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertSignup(signup: RaidSignupEntity): RaidSignupEntity {
        val sql = """
            INSERT INTO raid_signups (
                raid_id, character_id, character_name, character_realm,
                character_region, character_class, character_role, character_guest,
                status, comment, selected
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, signup.raidId)
            signup.characterId?.let { ps.setLong(2, it) } ?: ps.setNull(2, java.sql.Types.BIGINT)
            signup.characterName?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            signup.characterRealm?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            signup.characterRegion?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            signup.characterClass?.let { ps.setString(6, it) } ?: ps.setNull(6, java.sql.Types.VARCHAR)
            signup.characterRole?.let { ps.setString(7, it) } ?: ps.setNull(7, java.sql.Types.VARCHAR)
            signup.characterGuest?.let { ps.setBoolean(8, it) } ?: ps.setNull(8, java.sql.Types.BOOLEAN)
            signup.status?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            signup.comment?.let { ps.setString(10, it) } ?: ps.setNull(10, java.sql.Types.VARCHAR)
            signup.selected?.let { ps.setBoolean(11, it) } ?: ps.setNull(11, java.sql.Types.BOOLEAN)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return signup.copy(id = generatedId?.toLong())
    }

    private fun updateSignup(signup: RaidSignupEntity) {
        val sql = """
            UPDATE raid_signups SET
                raid_id = ?,
                character_id = ?,
                character_name = ?,
                character_realm = ?,
                character_region = ?,
                character_class = ?,
                character_role = ?,
                character_guest = ?,
                status = ?,
                comment = ?,
                selected = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            signup.raidId,
            signup.characterId,
            signup.characterName,
            signup.characterRealm,
            signup.characterRegion,
            signup.characterClass,
            signup.characterRole,
            signup.characterGuest,
            signup.status,
            signup.comment,
            signup.selected,
            signup.id,
        )
    }

    private val signupRowMapper = RowMapper { rs, _ ->
        val characterIdValue = rs.getLong("character_id")
        val characterId = if (rs.wasNull()) null else characterIdValue

        val characterGuestValue = rs.getBoolean("character_guest")
        val characterGuest = if (rs.wasNull()) null else characterGuestValue

        val selectedValue = rs.getBoolean("selected")
        val selected = if (rs.wasNull()) null else selectedValue

        RaidSignupEntity(
            id = rs.getLong("id"),
            raidId = rs.getLong("raid_id"),
            characterId = characterId,
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            characterRegion = rs.getString("character_region"),
            characterClass = rs.getString("character_class"),
            characterRole = rs.getString("character_role"),
            characterGuest = characterGuest,
            status = rs.getString("status"),
            comment = rs.getString("comment"),
            selected = selected,
        )
    }
}
