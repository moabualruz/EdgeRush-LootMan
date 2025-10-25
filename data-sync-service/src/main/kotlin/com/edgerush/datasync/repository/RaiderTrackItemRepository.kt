package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderTrackItemRepository : CrudRepository<RaiderTrackItemEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
