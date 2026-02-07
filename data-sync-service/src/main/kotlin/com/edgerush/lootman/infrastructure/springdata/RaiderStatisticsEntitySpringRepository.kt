package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderStatisticsEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderStatisticsEntitySpringRepository :
    CrudRepository<RaiderStatisticsEntity, Long>,
    PagingAndSortingRepository<RaiderStatisticsEntity, Long> {
    fun findByRaiderId(raiderId: Long): RaiderStatisticsEntity?

    fun findByRaiderIdIn(raiderIds: List<Long>): List<RaiderStatisticsEntity>

    fun existsByRaiderId(raiderId: Long): Boolean

    fun deleteByRaiderId(raiderId: Long)
}
