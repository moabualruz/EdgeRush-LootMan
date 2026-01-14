package com.edgerush.lootman.domain.loot.repository

import com.edgerush.datasync.entity.LootAwardBonusIdEntity

interface LootAwardBonusIdRepository {
    fun findById(id: Long): LootAwardBonusIdEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<LootAwardBonusIdEntity>
    fun count(): Long
    fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardBonusIdEntity>
    fun countByLootAwardId(lootAwardId: Long): Long
    fun save(entity: LootAwardBonusIdEntity): LootAwardBonusIdEntity
    fun delete(id: Long)
}
