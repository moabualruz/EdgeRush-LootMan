package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.AuditLogEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuditLogEntitySpringRepository :
    CrudRepository<AuditLogEntity, Long>,
    PagingAndSortingRepository<AuditLogEntity, Long> {

    fun findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType: String, entityId: String): List<AuditLogEntity>
    fun findByUserIdOrderByTimestampDesc(userId: String): List<AuditLogEntity>
    fun findByTimestampBetweenOrderByTimestampDesc(from: Instant, to: Instant): List<AuditLogEntity>
    fun findByOperationOrderByTimestampDesc(operation: String): List<AuditLogEntity>
}
