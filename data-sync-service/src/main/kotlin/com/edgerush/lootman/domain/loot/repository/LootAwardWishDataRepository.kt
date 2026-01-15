package com.edgerush.lootman.domain.loot.repository

import com.edgerush.datasync.entity.LootAwardWishDataEntity

interface LootAwardWishDataRepository {
    fun findById(id: Long): LootAwardWishDataEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootAwardWishDataEntity>

    fun count(): Long

    fun findByLootAwardId(
        lootAwardId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardWishDataEntity>

    fun countByLootAwardId(lootAwardId: Long): Long

    fun save(entity: LootAwardWishDataEntity): LootAwardWishDataEntity

    fun delete(id: Long)
}
