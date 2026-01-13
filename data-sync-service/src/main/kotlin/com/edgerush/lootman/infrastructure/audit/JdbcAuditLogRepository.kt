package com.edgerush.lootman.infrastructure.audit

import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditLogId
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

/**
 * JDBC implementation of AuditLogRepository.
 *
 * Persists audit log entries to the audit_logs table.
 */
@Repository
class JdbcAuditLogRepository(
    private val jdbcTemplate: JdbcTemplate
) : AuditLogRepository {

    override fun save(auditLog: AuditLog): AuditLog {
        val sql = """
            INSERT INTO audit_logs (
                timestamp, operation, entity_type, entity_id, user_id, username, is_admin_mode, request_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            Timestamp.from(auditLog.timestamp),
            auditLog.operation.name,
            auditLog.entityType,
            auditLog.entityId,
            auditLog.userId,
            auditLog.username,
            auditLog.isAdminMode,
            auditLog.requestId
        )

        return auditLog
    }

    override fun findByEntity(entityType: String, entityId: String): List<AuditLog> {
        val sql = """
            SELECT id, timestamp, operation, entity_type, entity_id, user_id, username, is_admin_mode, request_id
            FROM audit_logs
            WHERE entity_type = ? AND entity_id = ?
            ORDER BY timestamp DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, auditLogRowMapper, entityType, entityId)
    }

    override fun findByUserId(userId: String): List<AuditLog> {
        val sql = """
            SELECT id, timestamp, operation, entity_type, entity_id, user_id, username, is_admin_mode, request_id
            FROM audit_logs
            WHERE user_id = ?
            ORDER BY timestamp DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, auditLogRowMapper, userId)
    }

    override fun findByTimeRange(from: Instant, to: Instant): List<AuditLog> {
        val sql = """
            SELECT id, timestamp, operation, entity_type, entity_id, user_id, username, is_admin_mode, request_id
            FROM audit_logs
            WHERE timestamp >= ? AND timestamp <= ?
            ORDER BY timestamp DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, auditLogRowMapper, Timestamp.from(from), Timestamp.from(to))
    }

    override fun findByOperation(operation: AuditOperation): List<AuditLog> {
        val sql = """
            SELECT id, timestamp, operation, entity_type, entity_id, user_id, username, is_admin_mode, request_id
            FROM audit_logs
            WHERE operation = ?
            ORDER BY timestamp DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, auditLogRowMapper, operation.name)
    }

    private val auditLogRowMapper = RowMapper { rs, _ ->
        AuditLog(
            id = AuditLogId(rs.getLong("id")),
            timestamp = rs.getTimestamp("timestamp").toInstant(),
            operation = AuditOperation.valueOf(rs.getString("operation")),
            entityType = rs.getString("entity_type"),
            entityId = rs.getString("entity_id"),
            userId = rs.getString("user_id"),
            username = rs.getString("username"),
            isAdminMode = rs.getBoolean("is_admin_mode"),
            requestId = rs.getString("request_id")
        )
    }
}
