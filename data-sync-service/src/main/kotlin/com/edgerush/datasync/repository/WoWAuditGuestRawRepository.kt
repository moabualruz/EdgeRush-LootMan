package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditGuestRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditGuestRawRepository : CrudRepository<WoWAuditGuestRawEntity, Long>

