package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderGearItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderGearItemEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderGearItemEntitySpringRepository :
    CrudRepository<RaiderGearItemEntity, Long>,
    PagingAndSortingRepository<RaiderGearItemEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderGearItemEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderGearItemEntity>

    fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String): List<RaiderGearItemEntity>

    fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String, pageable: Pageable): Page<RaiderGearItemEntity>

    fun countByRaiderIdAndGearSet(raiderId: Long, gearSet: String): Long

    fun findByRaiderIdAndSlot(raiderId: Long, slot: String): List<RaiderGearItemEntity>

    fun deleteByRaiderId(raiderId: Long)

    fun deleteByRaiderIdAndGearSet(raiderId: Long, gearSet: String)
}
