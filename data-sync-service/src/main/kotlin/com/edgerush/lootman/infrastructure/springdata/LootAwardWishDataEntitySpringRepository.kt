package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for LootAwardWishDataEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface LootAwardWishDataEntitySpringRepository :
    CrudRepository<LootAwardWishDataEntity, Long>,
    PagingAndSortingRepository<LootAwardWishDataEntity, Long> {

    fun findByLootAwardId(lootAwardId: Long, pageable: Pageable): Page<LootAwardWishDataEntity>

    fun countByLootAwardId(lootAwardId: Long): Long

    fun findByLootAwardId(lootAwardId: Long): List<LootAwardWishDataEntity>

    fun deleteByLootAwardId(lootAwardId: Long)
}
