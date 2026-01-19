package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderCrestCountEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderCrestCountEntitySpringRepository :
    CrudRepository<RaiderCrestCountEntity, Long>,
    PagingAndSortingRepository<RaiderCrestCountEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderCrestCountEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderCrestCountEntity>

    fun findByRaiderIdAndCrestType(raiderId: Long, crestType: String): RaiderCrestCountEntity?

    fun deleteByRaiderId(raiderId: Long)
}
