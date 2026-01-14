package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderPvpBracketEntity

interface RaiderPvpBracketRepository {
    fun findById(id: Long): RaiderPvpBracketEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<RaiderPvpBracketEntity>
    fun count(): Long
    fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderPvpBracketEntity>
    fun countByRaiderId(raiderId: Long): Long
    fun save(entity: RaiderPvpBracketEntity): RaiderPvpBracketEntity
    fun delete(id: Long)
}
