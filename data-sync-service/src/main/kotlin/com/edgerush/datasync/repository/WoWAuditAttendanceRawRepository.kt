package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditAttendanceRawEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditAttendanceRawRepository : CrudRepository<WoWAuditAttendanceRawEntity, Long>

