package com.edgerush.lootman.domain.audit.repository

import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditOperation
import java.time.Instant

/**
 * Repository interface for AuditLog persistence.
 *
 * Provides methods to save and query audit log entries.
 */
interface AuditLogRepository {
    /**
     * Save an audit log entry.
     *
     * @param auditLog The audit log entry to save
     * @return The saved audit log entry
     */
    fun save(auditLog: AuditLog): AuditLog

    /**
     * Find audit logs by entity type and ID.
     *
     * @param entityType The type of entity (e.g., "Guild", "Raider")
     * @param entityId The entity's unique identifier
     * @return List of audit logs for the specified entity, ordered by timestamp descending
     */
    fun findByEntity(
        entityType: String,
        entityId: String,
    ): List<AuditLog>

    /**
     * Find audit logs by user ID.
     *
     * @param userId The user's unique identifier
     * @return List of audit logs for the specified user, ordered by timestamp descending
     */
    fun findByUserId(userId: String): List<AuditLog>

    /**
     * Find audit logs within a time range.
     *
     * @param from Start of the time range (inclusive)
     * @param to End of the time range (inclusive)
     * @return List of audit logs within the time range, ordered by timestamp descending
     */
    fun findByTimeRange(
        from: Instant,
        to: Instant,
    ): List<AuditLog>

    /**
     * Find audit logs by operation type.
     *
     * @param operation The operation type to filter by
     * @return List of audit logs with the specified operation, ordered by timestamp descending
     */
    fun findByOperation(operation: AuditOperation): List<AuditLog>
}
