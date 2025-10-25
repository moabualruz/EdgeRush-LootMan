package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderWarcraftLogRepository : CrudRepository<RaiderWarcraftLogEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
