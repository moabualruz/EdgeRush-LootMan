package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderTrackItemEntity

interface RaiderTrackItemRepository {
    fun findById(id: Long): RaiderTrackItemEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun save(entity: RaiderTrackItemEntity): RaiderTrackItemEntity

    fun delete(id: Long)
}
