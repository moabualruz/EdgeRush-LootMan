package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderEntity

interface RaiderEntityRepository {
    fun findById(id: Long): RaiderEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<RaiderEntity>
    fun count(): Long
    fun findByRealm(realm: String, offset: Long, limit: Int): List<RaiderEntity>
    fun countByRealm(realm: String): Long
    fun findByRegion(region: String, offset: Long, limit: Int): List<RaiderEntity>
    fun countByRegion(region: String): Long
    fun save(entity: RaiderEntity): RaiderEntity
    fun delete(id: Long)
}
