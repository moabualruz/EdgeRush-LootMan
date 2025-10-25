package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditTeamRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditTeamRawRepository : CrudRepository<WoWAuditTeamRawEntity, Long>

