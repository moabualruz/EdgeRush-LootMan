package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRole
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of UserRepository.
 *
 * Persists users to the users table.
 */
@Repository
class JdbcUserRepository(
    private val jdbcTemplate: JdbcTemplate
) : UserRepository {

    override fun findById(id: UserId): User? {
        val sql = """
            SELECT id, discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login
            FROM users
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, id.value).firstOrNull()
    }

    override fun findByDiscordId(discordId: String): User? {
        val sql = """
            SELECT id, discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login
            FROM users
            WHERE discord_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, discordId).firstOrNull()
    }

    override fun findByBattlenetId(battlenetId: String): User? {
        val sql = """
            SELECT id, discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login
            FROM users
            WHERE battlenet_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, battlenetId).firstOrNull()
    }

    override fun findByGuildId(guildId: GuildId): List<User> {
        val sql = """
            SELECT id, discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login
            FROM users
            WHERE guild_id = ?
            ORDER BY username ASC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, guildId.value)
    }

    override fun save(user: User): User {
        return if (user.id == null) {
            insert(user)
        } else {
            update(user)
        }
    }

    private fun insert(user: User): User {
        val sql = """
            INSERT INTO users (discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, user.discordId)
            ps.setString(2, user.battlenetId)
            ps.setString(3, user.username)
            ps.setString(4, user.email)
            ps.setString(5, user.avatarUrl)
            ps.setString(6, user.role.name)
            ps.setString(7, user.guildId?.value)
            ps.setTimestamp(8, Timestamp.from(user.createdAt))
            ps.setTimestamp(9, user.lastLogin?.let { Timestamp.from(it) })
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Long
            ?: throw IllegalStateException("Failed to retrieve generated ID for user")

        return user.withId(UserId(generatedId))
    }

    private fun update(user: User): User {
        val sql = """
            UPDATE users
            SET discord_id = ?, battlenet_id = ?, username = ?, email = ?, avatar_url = ?, role = ?, guild_id = ?, last_login = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            user.discordId,
            user.battlenetId,
            user.username,
            user.email,
            user.avatarUrl,
            user.role.name,
            user.guildId?.value,
            user.lastLogin?.let { Timestamp.from(it) },
            user.id!!.value
        )

        return user
    }

    override fun deleteById(id: UserId) {
        val sql = "DELETE FROM users WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun existsByDiscordId(discordId: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE discord_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Long::class.java, discordId)
        return (count ?: 0) > 0
    }

    override fun existsByBattlenetId(battlenetId: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE battlenet_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Long::class.java, battlenetId)
        return (count ?: 0) > 0
    }

    override fun findAll(offset: Long, limit: Int): List<User> {
        val sql = """
            SELECT id, discord_id, battlenet_id, username, email, avatar_url, role, guild_id, created_at, last_login
            FROM users
            ORDER BY id ASC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM users"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0
    }

    private val rowMapper = RowMapper { rs, _ ->
        User(
            id = UserId(rs.getLong("id")),
            discordId = rs.getString("discord_id"),
            battlenetId = rs.getString("battlenet_id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
            avatarUrl = rs.getString("avatar_url"),
            role = UserRole.fromString(rs.getString("role")),
            guildId = rs.getString("guild_id")?.let { GuildId(it) },
            createdAt = rs.getTimestamp("created_at").toInstant(),
            lastLogin = rs.getTimestamp("last_login")?.toInstant()
        )
    }
}
