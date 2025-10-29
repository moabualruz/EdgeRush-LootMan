package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderStatisticsRepository : CrudRepository<RaiderStatisticsEntity, Long> {
    fun findByRaiderId(raiderId: Long): RaiderStatisticsEntity?
}
