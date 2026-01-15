package com.edgerush.lootman.domain.statistics.repository

import com.edgerush.datasync.entity.RaiderStatisticsEntity

interface RaiderStatisticsRepository {
    fun findById(id: Long): RaiderStatisticsEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderStatisticsEntity>

    fun count(): Long

    fun findByRaiderId(raiderId: Long): RaiderStatisticsEntity?

    fun existsByRaiderId(raiderId: Long): Boolean

    fun save(entity: RaiderStatisticsEntity): RaiderStatisticsEntity

    fun delete(id: Long)
}
