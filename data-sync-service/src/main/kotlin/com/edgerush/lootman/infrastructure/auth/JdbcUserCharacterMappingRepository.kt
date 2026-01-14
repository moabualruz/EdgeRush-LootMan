package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of UserCharacterMappingRepository.
 */
@Repository
class JdbcUserCharacterMappingRepository(
    private val jdbcTemplate: JdbcTemplate
) : UserCharacterMappingRepository {

    override fun findById(id: UserCharacterMappingId): UserCharacterMapping? {
        val sql = """
            SELECT id, user_id, raider_id, is_primary, linked_at, verified, verified_at
            FROM user_character_mappings
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, id.value).firstOrNull()
    }

    override fun findByUserId(userId: UserId): List<UserCharacterMapping> {
        val sql = """
            SELECT id, user_id, raider_id, is_primary, linked_at, verified, verified_at
            FROM user_character_mappings
            WHERE user_id = ?
            ORDER BY is_primary DESC, linked_at ASC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, userId.value)
    }

    override fun findPrimaryByUserId(userId: UserId): UserCharacterMapping? {
        val sql = """
            SELECT id, user_id, raider_id, is_primary, linked_at, verified, verified_at
            FROM user_character_mappings
            WHERE user_id = ? AND is_primary = true
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, userId.value).firstOrNull()
    }

    override fun findByRaiderId(raiderId: RaiderId): List<UserCharacterMapping> {
        val sql = """
            SELECT id, user_id, raider_id, is_primary, linked_at, verified, verified_at
            FROM user_character_mappings
            WHERE raider_id = ?
            ORDER BY linked_at ASC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, raiderId.value)
    }

    override fun existsByUserIdAndRaiderId(userId: UserId, raiderId: RaiderId): Boolean {
        val sql = """
            SELECT COUNT(*) FROM user_character_mappings
            WHERE user_id = ? AND raider_id = ?
        """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Long::class.java, userId.value, raiderId.value)
        return (count ?: 0) > 0
    }

    override fun save(mapping: UserCharacterMapping): UserCharacterMapping {
        return if (mapping.id == null) {
            insert(mapping)
        } else {
            update(mapping)
        }
    }

    private fun insert(mapping: UserCharacterMapping): UserCharacterMapping {
        val sql = """
            INSERT INTO user_character_mappings (user_id, raider_id, is_primary, linked_at, verified, verified_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, mapping.userId.value)
            ps.setLong(2, mapping.raiderId.value)
            ps.setBoolean(3, mapping.isPrimary)
            ps.setTimestamp(4, Timestamp.from(mapping.linkedAt))
            ps.setBoolean(5, mapping.verified)
            ps.setTimestamp(6, mapping.verifiedAt?.let { Timestamp.from(it) })
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Long
            ?: throw IllegalStateException("Failed to retrieve generated ID for user_character_mapping")

        return mapping.withId(UserCharacterMappingId(generatedId))
    }

    private fun update(mapping: UserCharacterMapping): UserCharacterMapping {
        val sql = """
            UPDATE user_character_mappings
            SET user_id = ?, raider_id = ?, is_primary = ?, linked_at = ?, verified = ?, verified_at = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            mapping.userId.value,
            mapping.raiderId.value,
            mapping.isPrimary,
            Timestamp.from(mapping.linkedAt),
            mapping.verified,
            mapping.verifiedAt?.let { Timestamp.from(it) },
            mapping.id!!.value
        )

        return mapping
    }

    override fun deleteById(id: UserCharacterMappingId) {
        val sql = "DELETE FROM user_character_mappings WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun deleteByUserId(userId: UserId): Int {
        val sql = "DELETE FROM user_character_mappings WHERE user_id = ?"
        return jdbcTemplate.update(sql, userId.value)
    }

    override fun clearPrimaryForUser(userId: UserId) {
        val sql = """
            UPDATE user_character_mappings
            SET is_primary = false
            WHERE user_id = ? AND is_primary = true
        """.trimIndent()

        jdbcTemplate.update(sql, userId.value)
    }

    override fun countByUserId(userId: UserId): Long {
        val sql = "SELECT COUNT(*) FROM user_character_mappings WHERE user_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, userId.value) ?: 0
    }

    private val rowMapper = RowMapper { rs, _ ->
        UserCharacterMapping(
            id = UserCharacterMappingId(rs.getLong("id")),
            userId = UserId(rs.getLong("user_id")),
            raiderId = RaiderId(rs.getLong("raider_id")),
            isPrimary = rs.getBoolean("is_primary"),
            linkedAt = rs.getTimestamp("linked_at").toInstant(),
            verified = rs.getBoolean("verified"),
            verifiedAt = rs.getTimestamp("verified_at")?.toInstant()
        )
    }
}
