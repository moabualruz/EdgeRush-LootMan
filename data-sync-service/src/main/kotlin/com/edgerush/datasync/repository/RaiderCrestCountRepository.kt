package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderCrestCountRepository : CrudRepository<RaiderCrestCountEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
