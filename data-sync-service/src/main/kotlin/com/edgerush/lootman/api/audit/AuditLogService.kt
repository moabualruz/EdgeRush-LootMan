package com.edgerush.lootman.api.audit

import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Service for querying audit logs via the API layer.
 *
 * This service provides read-only access to audit logs for administrative
 * purposes. Audit logs are created internally by the system during CRUD
 * operations and cannot be modified through the API.
 */
@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
) {
    /**
     * Find audit logs by entity type and ID.
     *
     * @param entityType The type of entity (e.g., "Guild", "Raider")
     * @param entityId The entity's unique identifier
     * @return List of audit log responses
     */
    fun findByEntity(
        entityType: String,
        entityId: String,
    ): List<AuditLogResponse> {
        return auditLogRepository.findByEntity(entityType, entityId)
            .map { AuditLogResponse.from(it) }
    }

    /**
     * Find audit logs by user ID.
     *
     * @param userId The user's unique identifier
     * @return List of audit log responses
     */
    fun findByUserId(userId: String): List<AuditLogResponse> {
        return auditLogRepository.findByUserId(userId)
            .map { AuditLogResponse.from(it) }
    }

    /**
     * Find audit logs within a time range.
     *
     * @param from Start of the time range (inclusive)
     * @param to End of the time range (inclusive)
     * @return List of audit log responses
     */
    fun findByTimeRange(
        from: Instant,
        to: Instant,
    ): List<AuditLogResponse> {
        require(!from.isAfter(to)) { "Start time must not be after end time" }
        return auditLogRepository.findByTimeRange(from, to)
            .map { AuditLogResponse.from(it) }
    }

    /**
     * Find audit logs by operation type.
     *
     * @param operation The operation type to filter by
     * @return List of audit log responses
     */
    fun findByOperation(operation: AuditOperation): List<AuditLogResponse> {
        return auditLogRepository.findByOperation(operation)
            .map { AuditLogResponse.from(it) }
    }

    /**
     * Count audit logs by entity.
     *
     * @param entityType The type of entity
     * @param entityId The entity's unique identifier
     * @return Count of audit logs
     */
    fun countByEntity(
        entityType: String,
        entityId: String,
    ): Long {
        return auditLogRepository.findByEntity(entityType, entityId).size.toLong()
    }

    /**
     * Count audit logs by user.
     *
     * @param userId The user's unique identifier
     * @return Count of audit logs
     */
    fun countByUserId(userId: String): Long {
        return auditLogRepository.findByUserId(userId).size.toLong()
    }
}
