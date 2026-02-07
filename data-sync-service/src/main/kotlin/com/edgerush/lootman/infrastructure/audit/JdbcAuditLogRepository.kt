package com.edgerush.lootman.infrastructure.audit

import com.edgerush.datasync.entity.AuditLogEntity
import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditLogId
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import com.edgerush.lootman.infrastructure.springdata.AuditLogEntitySpringRepository
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * JDBC implementation of AuditLogRepository.
 *
 * Persists audit log entries using Spring Data JDBC.
 */
@Repository
class JdbcAuditLogRepository(
    private val springRepository: AuditLogEntitySpringRepository,
) : AuditLogRepository {
    override fun save(auditLog: AuditLog): AuditLog {
        val entity = auditLog.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findByEntity(
        entityType: String,
        entityId: String,
    ): List<AuditLog> =
        springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            .map { it.toDomain() }

    override fun findByUserId(userId: String): List<AuditLog> =
        springRepository.findByUserIdOrderByTimestampDesc(userId).map { it.toDomain() }

    override fun findByTimeRange(
        from: Instant,
        to: Instant,
    ): List<AuditLog> = springRepository.findByTimestampBetweenOrderByTimestampDesc(from, to).map { it.toDomain() }

    override fun findByOperation(operation: AuditOperation): List<AuditLog> =
        springRepository.findByOperationOrderByTimestampDesc(operation.name).map { it.toDomain() }

    private fun AuditLogEntity.toDomain(): AuditLog =
        AuditLog(
            id = id?.let { AuditLogId(it) },
            timestamp = timestamp,
            operation = AuditOperation.valueOf(operation),
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = username,
            isAdminMode = isAdminMode,
            requestId = requestId,
        )

    private fun AuditLog.toEntity(): AuditLogEntity =
        AuditLogEntity(
            id = id?.value,
            timestamp = timestamp,
            operation = operation.name,
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = username,
            isAdminMode = isAdminMode,
            requestId = requestId,
        )
}
