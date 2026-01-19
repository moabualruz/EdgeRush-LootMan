package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderVaultSlotEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderVaultSlotEntitySpringRepository :
    CrudRepository<RaiderVaultSlotEntity, Long>,
    PagingAndSortingRepository<RaiderVaultSlotEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<RaiderVaultSlotEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderVaultSlotEntity>

    fun findByRaiderIdAndSlot(raiderId: Long, slot: String): List<RaiderVaultSlotEntity>

    fun findByRaiderIdAndUnlockedTrue(raiderId: Long, pageable: Pageable): Page<RaiderVaultSlotEntity>

    fun countByRaiderIdAndUnlockedTrue(raiderId: Long): Long

    fun deleteByRaiderId(raiderId: Long)
}
