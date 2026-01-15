package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderCrestCountEntity

interface RaiderCrestCountRepository {
    fun findById(id: Long): RaiderCrestCountEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderCrestCountEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderCrestCountEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun save(entity: RaiderCrestCountEntity): RaiderCrestCountEntity

    fun delete(id: Long)
}
