package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderWarcraftLogEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderWarcraftLogEntitySpringRepository :
    CrudRepository<RaiderWarcraftLogEntity, Long>,
    PagingAndSortingRepository<RaiderWarcraftLogEntity, Long> {
    fun findByRaiderId(
        raiderId: Long,
        pageable: Pageable,
    ): Page<RaiderWarcraftLogEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderWarcraftLogEntity>

    fun findByRaiderIdAndDifficulty(
        raiderId: Long,
        difficulty: String,
    ): List<RaiderWarcraftLogEntity>

    fun deleteByRaiderId(raiderId: Long)
}
