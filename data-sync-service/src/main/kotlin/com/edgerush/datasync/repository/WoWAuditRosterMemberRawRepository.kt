package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditRosterMemberRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditRosterMemberRawRepository : CrudRepository<WoWAuditRosterMemberRawEntity, Long>

