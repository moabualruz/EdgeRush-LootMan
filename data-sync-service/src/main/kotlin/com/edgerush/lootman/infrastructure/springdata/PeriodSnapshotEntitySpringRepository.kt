package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for PeriodSnapshotEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface PeriodSnapshotEntitySpringRepository :
    CrudRepository<PeriodSnapshotEntity, Long>,
    PagingAndSortingRepository<PeriodSnapshotEntity, Long> {

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<PeriodSnapshotEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByTeamId(teamId: Long): List<PeriodSnapshotEntity>

    fun findByTeamIdAndSeasonId(teamId: Long, seasonId: Long, pageable: Pageable): Page<PeriodSnapshotEntity>

    fun countByTeamIdAndSeasonId(teamId: Long, seasonId: Long): Long

    fun findByTeamIdAndSeasonIdAndPeriodId(teamId: Long, seasonId: Long, periodId: Long): PeriodSnapshotEntity?

    fun findByCurrentPeriodTrue(): List<PeriodSnapshotEntity>
}
