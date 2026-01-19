package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderTrackItemEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderTrackItemEntitySpringRepository :
    CrudRepository<RaiderTrackItemEntity, Long>,
    PagingAndSortingRepository<RaiderTrackItemEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderTrackItemEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderTrackItemEntity>

    fun findByRaiderIdAndTier(raiderId: Long, tier: String): RaiderTrackItemEntity?

    fun deleteByRaiderId(raiderId: Long)
}
