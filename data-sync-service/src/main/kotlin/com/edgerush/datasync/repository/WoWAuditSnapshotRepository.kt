package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WoWAuditSnapshotEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WoWAuditSnapshotRepository : CrudRepository<WoWAuditSnapshotEntity, Long>
