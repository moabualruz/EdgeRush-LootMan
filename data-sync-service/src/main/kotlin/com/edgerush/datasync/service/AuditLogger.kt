package com.edgerush.datasync.service

import com.edgerush.datasync.entity.AuditLogEntity
import com.edgerush.datasync.repository.AuditLogRepository
import com.edgerush.datasync.security.AuthenticatedUser
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditLogger(
    private val auditRepository: AuditLogRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun logCreate(
        entityType: String,
        entityId: Any,
        user: AuthenticatedUser,
    ) {
        log("CREATE", entityType, entityId, user)
    }

    fun logUpdate(
        entityType: String,
        entityId: Any,
        user: AuthenticatedUser,
    ) {
        log("UPDATE", entityType, entityId, user)
    }

    fun logDelete(
        entityType: String,
        entityId: Any,
        user: AuthenticatedUser,
    ) {
        log("DELETE", entityType, entityId, user)
    }

    fun logAccess(
        entityType: String,
        entityId: Any,
        user: AuthenticatedUser,
    ) {
        log("ACCESS", entityType, entityId, user)
    }

    private fun log(
        operation: String,
        entityType: String,
        entityId: Any,
        user: AuthenticatedUser,
    ) {
        try {
            val entry =
                AuditLogEntity(
                    timestamp = LocalDateTime.now(),
                    operation = operation,
                    entityType = entityType,
                    entityId = entityId.toString(),
                    userId = user.id,
                    username = user.username,
                    isAdminMode = user.isAdminMode,
                    requestId = MDC.get("requestId"),
                )
            auditRepository.save(entry)

            logger.info(
                "AUDIT: {} {} {} by user {} (admin_mode: {})",
                operation,
                entityType,
                entityId,
                user.username,
                user.isAdminMode,
            )
        } catch (e: Exception) {
            logger.error("Failed to write audit log", e)
        }
    }
}
