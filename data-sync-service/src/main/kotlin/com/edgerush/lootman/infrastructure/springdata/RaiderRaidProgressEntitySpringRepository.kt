package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderRaidProgressEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderRaidProgressEntitySpringRepository :
    CrudRepository<RaiderRaidProgressEntity, Long>,
    PagingAndSortingRepository<RaiderRaidProgressEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderRaidProgressEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderRaidProgressEntity>

    fun findByRaiderIdAndRaid(raiderId: Long, raid: String): RaiderRaidProgressEntity?

    fun findByRaiderIdAndDifficulty(raiderId: Long, difficulty: String): List<RaiderRaidProgressEntity>

    fun deleteByRaiderId(raiderId: Long)
}
