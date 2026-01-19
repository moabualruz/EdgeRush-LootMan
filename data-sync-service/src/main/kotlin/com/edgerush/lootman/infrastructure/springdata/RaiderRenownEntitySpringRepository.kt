package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderRenownEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderRenownEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderRenownEntitySpringRepository :
    CrudRepository<RaiderRenownEntity, Long>,
    PagingAndSortingRepository<RaiderRenownEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderRenownEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderRenownEntity>

    fun findByRaiderIdAndFaction(raiderId: Long, faction: String): RaiderRenownEntity?

    fun deleteByRaiderId(raiderId: Long)
}
