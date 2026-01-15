package com.edgerush.lootman.api.audit

import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditOperation
import java.time.Instant

/**
 * Response DTO for AuditLog.
 */
data class AuditLogResponse(
    val id: Long,
    val timestamp: Instant,
    val operation: AuditOperation,
    val entityType: String,
    val entityId: String,
    val userId: String,
    val username: String,
    val isAdminMode: Boolean,
    val requestId: String?,
) {
    companion object {
        fun from(auditLog: AuditLog): AuditLogResponse {
            return AuditLogResponse(
                id = auditLog.id?.value ?: throw IllegalStateException("AuditLog ID must not be null"),
                timestamp = auditLog.timestamp,
                operation = auditLog.operation,
                entityType = auditLog.entityType,
                entityId = auditLog.entityId,
                userId = auditLog.userId,
                username = auditLog.username,
                isAdminMode = auditLog.isAdminMode,
                requestId = auditLog.requestId,
            )
        }
    }
}

/**
 * Response for count queries.
 */
data class AuditLogCountResponse(
    val count: Long,
)
