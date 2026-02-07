package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaidSignupEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaidSignupEntitySpringRepository :
    CrudRepository<RaidSignupEntity, Long>,
    PagingAndSortingRepository<RaidSignupEntity, Long> {
    fun findByRaidId(
        raidId: Long,
        pageable: Pageable,
    ): Page<RaidSignupEntity>

    fun countByRaidId(raidId: Long): Long

    fun findByRaidId(raidId: Long): List<RaidSignupEntity>

    fun findByCharacterId(
        characterId: Long,
        pageable: Pageable,
    ): Page<RaidSignupEntity>

    fun countByCharacterId(characterId: Long): Long

    fun findByRaidIdAndCharacterId(
        raidId: Long,
        characterId: Long,
    ): RaidSignupEntity?

    fun deleteByRaidId(raidId: Long)

    fun findByRaidIdAndSelectedTrue(
        raidId: Long,
        pageable: Pageable,
    ): Page<RaidSignupEntity>

    fun countByRaidIdAndSelectedTrue(raidId: Long): Long
}
