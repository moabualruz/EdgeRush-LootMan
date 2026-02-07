package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderPvpBracketEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderPvpBracketEntitySpringRepository :
    CrudRepository<RaiderPvpBracketEntity, Long>,
    PagingAndSortingRepository<RaiderPvpBracketEntity, Long> {
    fun findByRaiderId(
        raiderId: Long,
        pageable: Pageable,
    ): Page<RaiderPvpBracketEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<RaiderPvpBracketEntity>

    fun findByRaiderIdAndBracket(
        raiderId: Long,
        bracket: String,
    ): RaiderPvpBracketEntity?

    fun deleteByRaiderId(raiderId: Long)
}
