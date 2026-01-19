package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaidEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Spring Data JDBC repository for RaidEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaidEntitySpringRepository :
    CrudRepository<RaidEntity, Long>,
    PagingAndSortingRepository<RaidEntity, Long> {

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<RaidEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByRaidId(raidId: Long): RaidEntity?

    fun existsByRaidId(raidId: Long): Boolean

    fun deleteByRaidId(raidId: Long)

    @Query(
        """
        SELECT * FROM raids
        WHERE team_id = :teamId AND start_time BETWEEN :startDate AND :endDate
        ORDER BY start_time DESC
        """
    )
    fun findByTeamIdAndDateRange(
        teamId: Long,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
    ): List<RaidEntity>

    @Query(
        """
        SELECT COUNT(*) FROM raids
        WHERE team_id = :teamId AND start_time BETWEEN :startDate AND :endDate
        """
    )
    fun countByTeamIdAndDateRange(
        teamId: Long,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
    ): Long

    @Query(
        """
        SELECT * FROM raids
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC, raid_id DESC
        """
    )
    fun findByDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<RaidEntity>

    @Query(
        """
        SELECT COUNT(*) FROM raids
        WHERE date >= :startDate AND date <= :endDate
        """
    )
    fun countByDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Long

    @Query(
        """
        SELECT r.* FROM raids r
        INNER JOIN team_metadata tm ON r.team_id = tm.team_id
        WHERE tm.guild_id = :guildId AND r.date >= CURRENT_DATE
        ORDER BY r.date ASC, r.start_time ASC
        LIMIT :limit
        """
    )
    fun findUpcomingByGuildId(guildId: Long, limit: Int): List<RaidEntity>

    @Query(
        """
        SELECT r.* FROM raids r
        INNER JOIN team_metadata tm ON r.team_id = tm.team_id
        WHERE tm.guild_id = :guildId AND r.date < CURRENT_DATE
        ORDER BY r.date DESC, r.start_time DESC
        LIMIT :limit
        """
    )
    fun findPastByGuildId(guildId: Long, limit: Int): List<RaidEntity>

    fun findByInstance(instance: String, pageable: Pageable): Page<RaidEntity>

    fun countByInstance(instance: String): Long
}
