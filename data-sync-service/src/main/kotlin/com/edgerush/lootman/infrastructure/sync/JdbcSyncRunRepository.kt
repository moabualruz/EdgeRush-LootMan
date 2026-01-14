package com.edgerush.lootman.infrastructure.sync

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of SyncRunRepository.
 *
 * Persists SyncRunEntity to the sync_runs table.
 */
@Repository
class JdbcSyncRunRepository(
    private val jdbcTemplate: JdbcTemplate,
) : SyncRunRepository {

    override fun findById(id: Long): SyncRunEntity? {
        val sql = """
            SELECT id, source, status, started_at, completed_at, message
            FROM sync_runs
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, syncRunRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM sync_runs WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<SyncRunEntity> {
        val sql = """
            SELECT id, source, status, started_at, completed_at, message
            FROM sync_runs
            ORDER BY started_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, syncRunRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM sync_runs"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findBySource(source: String, offset: Long, limit: Int): List<SyncRunEntity> {
        val sql = """
            SELECT id, source, status, started_at, completed_at, message
            FROM sync_runs
            WHERE source = ?
            ORDER BY started_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, syncRunRowMapper, source, limit, offset)
    }

    override fun countBySource(source: String): Long {
        val sql = "SELECT COUNT(*) FROM sync_runs WHERE source = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, source) ?: 0L
    }

    override fun findByStatus(status: String, offset: Long, limit: Int): List<SyncRunEntity> {
        val sql = """
            SELECT id, source, status, started_at, completed_at, message
            FROM sync_runs
            WHERE status = ?
            ORDER BY started_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, syncRunRowMapper, status, limit, offset)
    }

    override fun countByStatus(status: String): Long {
        val sql = "SELECT COUNT(*) FROM sync_runs WHERE status = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, status) ?: 0L
    }

    override fun save(entity: SyncRunEntity): SyncRunEntity {
        return if (entity.id == null) {
            insertSyncRun(entity)
        } else {
            updateSyncRun(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM sync_runs WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertSyncRun(entity: SyncRunEntity): SyncRunEntity {
        val sql = """
            INSERT INTO sync_runs (source, status, started_at, completed_at, message)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, entity.source)
            ps.setString(2, entity.status)
            ps.setTimestamp(3, Timestamp.from(entity.startedAt.toInstant()))
            entity.completedAt?.let { ps.setTimestamp(4, Timestamp.from(it.toInstant())) }
                ?: ps.setNull(4, java.sql.Types.TIMESTAMP)
            entity.message?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateSyncRun(entity: SyncRunEntity) {
        val sql = """
            UPDATE sync_runs SET
                source = ?, status = ?, started_at = ?, completed_at = ?, message = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.source,
            entity.status,
            Timestamp.from(entity.startedAt.toInstant()),
            entity.completedAt?.let { Timestamp.from(it.toInstant()) },
            entity.message,
            entity.id,
        )
    }

    private val syncRunRowMapper = RowMapper { rs, _ ->
        SyncRunEntity(
            id = rs.getLong("id"),
            source = rs.getString("source"),
            status = rs.getString("status"),
            startedAt = rs.getTimestamp("started_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            completedAt = rs.getTimestamp("completed_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
            message = rs.getString("message"),
        )
    }
}
