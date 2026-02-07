package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaidEncounterEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaidEncounterEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaidEncounterEntitySpringRepository :
    CrudRepository<RaidEncounterEntity, Long>,
    PagingAndSortingRepository<RaidEncounterEntity, Long> {
    fun findByRaidId(
        raidId: Long,
        pageable: Pageable,
    ): Page<RaidEncounterEntity>

    fun countByRaidId(raidId: Long): Long

    fun findByRaidId(raidId: Long): List<RaidEncounterEntity>

    fun findByEncounterId(
        encounterId: Long,
        pageable: Pageable,
    ): Page<RaidEncounterEntity>

    fun countByEncounterId(encounterId: Long): Long

    fun findByRaidIdAndEncounterId(
        raidId: Long,
        encounterId: Long,
    ): RaidEncounterEntity?

    fun deleteByRaidId(raidId: Long)

    fun findByRaidIdAndEnabledTrue(
        raidId: Long,
        pageable: Pageable,
    ): Page<RaidEncounterEntity>

    fun countByRaidIdAndEnabledTrue(raidId: Long): Long
}
