package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaiderPvpBracketRepository : CrudRepository<RaiderPvpBracketEntity, Long> {
    fun deleteByRaiderId(raiderId: Long)
}
