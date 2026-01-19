package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("audit_logs")
data class AuditLogEntity(
    @Id
    val id: Long? = null,
    @Column("timestamp")
    val timestamp: Instant,
    @Column("operation")
    val operation: String,
    @Column("entity_type")
    val entityType: String,
    @Column("entity_id")
    val entityId: String,
    @Column("user_id")
    val userId: String,
    @Column("username")
    val username: String,
    @Column("is_admin_mode")
    val isAdminMode: Boolean = false,
    @Column("request_id")
    val requestId: String?,
)
