package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderRenownEntity

interface RaiderRenownRepository {
    fun findById(id: Long): RaiderRenownEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderRenownEntity>

    fun count(): Long

    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderRenownEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun save(entity: RaiderRenownEntity): RaiderRenownEntity

    fun delete(id: Long)
}
