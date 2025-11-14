package com.edgerush.datasync.api.dto.response

import java.time.LocalDateTime

data class AuditLogResponse(
    val id: Long?,
    val timestamp: LocalDateTime,
    val operation: String,
    val entityType: String,
    val entityId: String,
    val userId: String,
    val username: String,
    val isAdminMode: Boolean,
    val requestId: String?,
)
