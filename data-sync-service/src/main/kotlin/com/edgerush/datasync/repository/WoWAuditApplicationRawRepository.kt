package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditApplicationRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditApplicationRawRepository : CrudRepository<WoWAuditApplicationRawEntity, Long>

