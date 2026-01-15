package com.edgerush.lootman.domain.gear.repository

import com.edgerush.datasync.entity.RaiderGearItemEntity

interface RaiderGearItemRepository {
    fun findById(id: Long): RaiderGearItemEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderGearItemEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderGearItemEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderIdAndGearSet(
        raiderId: Long,
        gearSet: String,
        offset: Long,
        limit: Int,
    ): List<RaiderGearItemEntity>

    fun countByRaiderIdAndGearSet(
        raiderId: Long,
        gearSet: String,
    ): Long

    fun save(entity: RaiderGearItemEntity): RaiderGearItemEntity

    fun delete(id: Long)
}
