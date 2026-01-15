package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity

interface RaiderWarcraftLogRepository {
    fun findById(id: Long): RaiderWarcraftLogEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderWarcraftLogEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderWarcraftLogEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun save(entity: RaiderWarcraftLogEntity): RaiderWarcraftLogEntity

    fun delete(id: Long)
}
