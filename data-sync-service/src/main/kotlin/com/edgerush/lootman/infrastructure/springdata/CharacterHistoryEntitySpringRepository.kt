package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.CharacterHistoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for CharacterHistoryEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface CharacterHistoryEntitySpringRepository :
    CrudRepository<CharacterHistoryEntity, Long>,
    PagingAndSortingRepository<CharacterHistoryEntity, Long> {
    fun findByTeamId(
        teamId: Long,
        pageable: Pageable,
    ): Page<CharacterHistoryEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByCharacterId(
        characterId: Long,
        pageable: Pageable,
    ): Page<CharacterHistoryEntity>

    fun countByCharacterId(characterId: Long): Long

    fun findByCharacterId(characterId: Long): List<CharacterHistoryEntity>

    // Derived query method - Spring Data JDBC generates query from method name
    fun findByTeamIdAndSeasonIdAndPeriodId(
        teamId: Long,
        seasonId: Long,
        periodId: Long,
        pageable: Pageable,
    ): Page<CharacterHistoryEntity>

    @Query(
        """
        SELECT * FROM character_history
        WHERE character_id = :characterId AND season_id = :seasonId AND period_id = :periodId
        """,
    )
    fun findByCharacterIdAndSeasonIdAndPeriodId(
        characterId: Long,
        seasonId: Long,
        periodId: Long,
    ): CharacterHistoryEntity?

    fun findBySeasonIdAndPeriodId(
        seasonId: Long,
        periodId: Long,
        pageable: Pageable,
    ): Page<CharacterHistoryEntity>

    fun countBySeasonIdAndPeriodId(
        seasonId: Long,
        periodId: Long,
    ): Long
}
