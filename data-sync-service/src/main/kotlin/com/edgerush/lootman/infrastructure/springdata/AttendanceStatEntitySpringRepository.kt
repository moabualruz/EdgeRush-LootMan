package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.AttendanceStatEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for AttendanceStatEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface AttendanceStatEntitySpringRepository :
    CrudRepository<AttendanceStatEntity, Long>,
    PagingAndSortingRepository<AttendanceStatEntity, Long> {

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<AttendanceStatEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByTeamIdAndSeasonId(teamId: Long, seasonId: Long, pageable: Pageable): Page<AttendanceStatEntity>

    fun countByTeamIdAndSeasonId(teamId: Long, seasonId: Long): Long

    fun findByTeamIdAndSeasonIdAndPeriodId(
        teamId: Long,
        seasonId: Long,
        periodId: Long,
        pageable: Pageable,
    ): Page<AttendanceStatEntity>

    fun countByTeamIdAndSeasonIdAndPeriodId(teamId: Long, seasonId: Long, periodId: Long): Long

    fun findByCharacterId(characterId: Long): List<AttendanceStatEntity>

    fun findByCharacterId(characterId: Long, pageable: Pageable): Page<AttendanceStatEntity>

    fun countByCharacterId(characterId: Long): Long

    fun findBySeasonId(seasonId: Long, pageable: Pageable): Page<AttendanceStatEntity>

    fun countBySeasonId(seasonId: Long): Long

    @Query(
        """
        SELECT * FROM attendance_stats
        WHERE team_id = :teamId AND character_name = :characterName
        ORDER BY synced_at DESC
        """
    )
    fun findByTeamIdAndCharacterName(teamId: Long, characterName: String): List<AttendanceStatEntity>

    @Query(
        """
        SELECT * FROM attendance_stats
        WHERE team_id = :teamId AND season_id = :seasonId AND period_id = :periodId AND character_name = :characterName
        """
    )
    fun findByTeamIdAndSeasonIdAndPeriodIdAndCharacterName(
        teamId: Long,
        seasonId: Long,
        periodId: Long,
        characterName: String,
    ): AttendanceStatEntity?
}
