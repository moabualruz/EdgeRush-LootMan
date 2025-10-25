package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderVaultSlotRepository : CrudRepository<RaiderVaultSlotEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
