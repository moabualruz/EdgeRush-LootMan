package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.TeamRaidDayEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for TeamRaidDayEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface TeamRaidDayEntitySpringRepository :
    CrudRepository<TeamRaidDayEntity, Long>,
    PagingAndSortingRepository<TeamRaidDayEntity, Long> {
    fun findByTeamId(
        teamId: Long,
        pageable: Pageable,
    ): Page<TeamRaidDayEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByTeamId(teamId: Long): List<TeamRaidDayEntity>

    fun findByTeamIdAndWeekDay(
        teamId: Long,
        weekDay: Int,
    ): TeamRaidDayEntity?

    fun deleteByTeamId(teamId: Long)
}
