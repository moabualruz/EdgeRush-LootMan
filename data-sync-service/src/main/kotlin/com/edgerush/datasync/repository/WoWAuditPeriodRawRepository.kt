package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditPeriodRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditPeriodRawRepository : CrudRepository<WoWAuditPeriodRawEntity, Long>

