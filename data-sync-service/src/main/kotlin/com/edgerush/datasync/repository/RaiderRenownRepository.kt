package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderRenownEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderRenownRepository : CrudRepository<RaiderRenownEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
