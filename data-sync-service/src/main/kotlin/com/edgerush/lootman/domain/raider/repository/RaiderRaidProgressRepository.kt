package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderRaidProgressEntity

interface RaiderRaidProgressRepository {
    fun findById(id: Long): RaiderRaidProgressEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderRaidProgressEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderRaidProgressEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun save(entity: RaiderRaidProgressEntity): RaiderRaidProgressEntity

    fun delete(id: Long)
}
