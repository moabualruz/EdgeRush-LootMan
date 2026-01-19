package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for LootAwardBonusIdEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface LootAwardBonusIdEntitySpringRepository :
    CrudRepository<LootAwardBonusIdEntity, Long>,
    PagingAndSortingRepository<LootAwardBonusIdEntity, Long> {

    fun findByLootAwardId(lootAwardId: Long, pageable: Pageable): Page<LootAwardBonusIdEntity>

    fun countByLootAwardId(lootAwardId: Long): Long

    fun findByLootAwardId(lootAwardId: Long): List<LootAwardBonusIdEntity>

    fun deleteByLootAwardId(lootAwardId: Long)
}
