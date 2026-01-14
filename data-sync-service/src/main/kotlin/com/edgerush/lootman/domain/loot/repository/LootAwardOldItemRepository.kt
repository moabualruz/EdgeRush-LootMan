package com.edgerush.lootman.domain.loot.repository

import com.edgerush.datasync.entity.LootAwardOldItemEntity

interface LootAwardOldItemRepository {
    fun findById(id: Long): LootAwardOldItemEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<LootAwardOldItemEntity>
    fun count(): Long
    fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardOldItemEntity>
    fun countByLootAwardId(lootAwardId: Long): Long
    fun save(entity: LootAwardOldItemEntity): LootAwardOldItemEntity
    fun delete(id: Long)
}
