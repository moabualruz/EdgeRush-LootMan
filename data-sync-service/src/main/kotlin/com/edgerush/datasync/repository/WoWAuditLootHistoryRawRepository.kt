package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditLootHistoryRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditLootHistoryRawRepository : CrudRepository<WoWAuditLootHistoryRawEntity, Long>

