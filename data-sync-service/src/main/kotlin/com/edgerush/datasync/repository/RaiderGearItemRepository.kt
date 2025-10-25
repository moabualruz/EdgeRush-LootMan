package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderGearItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderGearItemRepository : CrudRepository<RaiderGearItemEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
