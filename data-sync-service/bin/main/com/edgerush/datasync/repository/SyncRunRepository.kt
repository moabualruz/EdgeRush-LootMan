package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.SyncRunEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncRunRepository : CrudRepository<SyncRunEntity, Long>
