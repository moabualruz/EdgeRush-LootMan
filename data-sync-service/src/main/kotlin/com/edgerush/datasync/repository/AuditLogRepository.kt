package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.AuditLogEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : CrudRepository<AuditLogEntity, Long>
