package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditRaidRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditRaidRawRepository : CrudRepository<WoWAuditRaidRawEntity, Long>

