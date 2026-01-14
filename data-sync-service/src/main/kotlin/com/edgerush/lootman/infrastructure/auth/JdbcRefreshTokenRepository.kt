package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.RefreshTokenId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken
import com.edgerush.lootman.domain.auth.repository.RefreshTokenRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of RefreshTokenRepository.
 *
 * Persists refresh tokens to the user_refresh_tokens table.
 */
@Repository
class JdbcRefreshTokenRepository(
    private val jdbcTemplate: JdbcTemplate
) : RefreshTokenRepository {

    override fun findById(id: RefreshTokenId): UserRefreshToken? {
        val sql = """
            SELECT id, user_id, token_hash, expires_at, created_at, revoked_at
            FROM user_refresh_tokens
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, id.value).firstOrNull()
    }

    override fun findByTokenHash(tokenHash: String): UserRefreshToken? {
        val sql = """
            SELECT id, user_id, token_hash, expires_at, created_at, revoked_at
            FROM user_refresh_tokens
            WHERE token_hash = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, tokenHash).firstOrNull()
    }

    override fun findByUserId(userId: UserId): List<UserRefreshToken> {
        val sql = """
            SELECT id, user_id, token_hash, expires_at, created_at, revoked_at
            FROM user_refresh_tokens
            WHERE user_id = ?
            ORDER BY created_at DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, userId.value)
    }

    override fun findValidByUserId(userId: UserId): List<UserRefreshToken> {
        val sql = """
            SELECT id, user_id, token_hash, expires_at, created_at, revoked_at
            FROM user_refresh_tokens
            WHERE user_id = ? AND revoked_at IS NULL AND expires_at > NOW()
            ORDER BY created_at DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, userId.value)
    }

    override fun save(token: UserRefreshToken): UserRefreshToken {
        return if (token.id == null) {
            insert(token)
        } else {
            update(token)
        }
    }

    private fun insert(token: UserRefreshToken): UserRefreshToken {
        val sql = """
            INSERT INTO user_refresh_tokens (user_id, token_hash, expires_at, created_at, revoked_at)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, token.userId.value)
            ps.setString(2, token.tokenHash)
            ps.setTimestamp(3, Timestamp.from(token.expiresAt))
            ps.setTimestamp(4, Timestamp.from(token.createdAt))
            ps.setTimestamp(5, token.revokedAt?.let { Timestamp.from(it) })
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Long
            ?: throw IllegalStateException("Failed to retrieve generated ID for refresh_token")

        return token.withId(RefreshTokenId(generatedId))
    }

    private fun update(token: UserRefreshToken): UserRefreshToken {
        val sql = """
            UPDATE user_refresh_tokens
            SET user_id = ?, token_hash = ?, expires_at = ?, revoked_at = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            token.userId.value,
            token.tokenHash,
            Timestamp.from(token.expiresAt),
            token.revokedAt?.let { Timestamp.from(it) },
            token.id!!.value
        )

        return token
    }

    override fun deleteById(id: RefreshTokenId) {
        val sql = "DELETE FROM user_refresh_tokens WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun deleteByUserId(userId: UserId): Int {
        val sql = "DELETE FROM user_refresh_tokens WHERE user_id = ?"
        return jdbcTemplate.update(sql, userId.value)
    }

    override fun revokeAllByUserId(userId: UserId): Int {
        val sql = """
            UPDATE user_refresh_tokens
            SET revoked_at = NOW()
            WHERE user_id = ? AND revoked_at IS NULL
        """.trimIndent()

        return jdbcTemplate.update(sql, userId.value)
    }

    override fun deleteExpired(): Int {
        val sql = "DELETE FROM user_refresh_tokens WHERE expires_at < NOW()"
        return jdbcTemplate.update(sql)
    }

    private val rowMapper = RowMapper { rs, _ ->
        UserRefreshToken(
            id = RefreshTokenId(rs.getLong("id")),
            userId = UserId(rs.getLong("user_id")),
            tokenHash = rs.getString("token_hash"),
            expiresAt = rs.getTimestamp("expires_at").toInstant(),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            revokedAt = rs.getTimestamp("revoked_at")?.toInstant()
        )
    }
}
