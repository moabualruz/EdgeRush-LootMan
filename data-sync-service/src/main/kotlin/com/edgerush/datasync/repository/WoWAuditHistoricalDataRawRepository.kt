package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditHistoricalDataRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditHistoricalDataRawRepository : CrudRepository<WoWAuditHistoricalDataRawEntity, Long>

