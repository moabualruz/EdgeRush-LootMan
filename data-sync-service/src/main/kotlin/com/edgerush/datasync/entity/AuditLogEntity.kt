package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("audit_logs")
data class AuditLogEntity(
    @Id
    val id: Long? = null,
    val timestamp: LocalDateTime,
    val operation: String,
    val entityType: String,
    val entityId: String,
    val userId: String,
    val username: String,
    val isAdminMode: Boolean,
    val requestId: String?,
)
