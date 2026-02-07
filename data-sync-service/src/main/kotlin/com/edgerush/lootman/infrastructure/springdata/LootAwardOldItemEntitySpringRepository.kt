package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for LootAwardOldItemEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface LootAwardOldItemEntitySpringRepository :
    CrudRepository<LootAwardOldItemEntity, Long>,
    PagingAndSortingRepository<LootAwardOldItemEntity, Long> {
    fun findByLootAwardId(
        lootAwardId: Long,
        pageable: Pageable,
    ): Page<LootAwardOldItemEntity>

    fun countByLootAwardId(lootAwardId: Long): Long

    fun findByLootAwardId(lootAwardId: Long): List<LootAwardOldItemEntity>

    fun deleteByLootAwardId(lootAwardId: Long)
}
