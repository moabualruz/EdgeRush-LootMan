package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderRaidProgressRepository : CrudRepository<RaiderRaidProgressEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
