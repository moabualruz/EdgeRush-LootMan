package com.edgerush.lootman.domain.audit.model

import java.time.Instant

/**
 * Represents an audit log entry capturing operations performed on entities.
 *
 * Audit logs track who did what, when, and to which entity, providing
 * a complete audit trail for compliance and debugging purposes.
 */
data class AuditLog(
    val id: AuditLogId?,
    val timestamp: Instant,
    val operation: AuditOperation,
    val entityType: String,
    val entityId: String,
    val userId: String,
    val username: String,
    val isAdminMode: Boolean,
    val requestId: String?
) {
    companion object {
        /**
         * Factory method to create a new audit log entry.
         *
         * @throws IllegalArgumentException if required fields are blank
         */
        fun create(
            operation: AuditOperation,
            entityType: String,
            entityId: String,
            userId: String,
            username: String,
            isAdminMode: Boolean,
            requestId: String?
        ): AuditLog {
            require(entityType.isNotBlank()) { "Entity type must not be blank" }
            require(entityId.isNotBlank()) { "Entity ID must not be blank" }
            require(userId.isNotBlank()) { "User ID must not be blank" }
            require(username.isNotBlank()) { "Username must not be blank" }

            return AuditLog(
                id = null,
                timestamp = Instant.now(),
                operation = operation,
                entityType = entityType,
                entityId = entityId,
                userId = userId,
                username = username,
                isAdminMode = isAdminMode,
                requestId = requestId
            )
        }
    }
}

/**
 * Value object representing an audit log entry ID.
 *
 * @property value The unique identifier (must be positive)
 * @throws IllegalArgumentException if value is not positive
 */
@JvmInline
value class AuditLogId(val value: Long) {
    init {
        require(value > 0) { "Audit log ID must be positive" }
    }
}

/**
 * Enumeration of audit operation types.
 */
enum class AuditOperation {
    CREATE,
    READ,
    UPDATE,
    DELETE
}
