package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.HistoricalActivityEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for HistoricalActivityEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface HistoricalActivityEntitySpringRepository :
    CrudRepository<HistoricalActivityEntity, Long>,
    PagingAndSortingRepository<HistoricalActivityEntity, Long> {

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<HistoricalActivityEntity>

    fun countByTeamId(teamId: Long): Long

    fun findByCharacterId(characterId: Long, pageable: Pageable): Page<HistoricalActivityEntity>

    fun countByCharacterId(characterId: Long): Long

    @Query(
        """
        SELECT * FROM historical_activity
        WHERE character_name = :characterName AND character_realm = :characterRealm
        ORDER BY synced_at DESC
        """
    )
    fun findByCharacterNameAndRealm(characterName: String, characterRealm: String): List<HistoricalActivityEntity>

    @Query(
        """
        SELECT * FROM historical_activity
        WHERE team_id = :teamId AND character_name = :characterName
        ORDER BY synced_at DESC
        """
    )
    fun findByTeamIdAndCharacterName(teamId: Long, characterName: String): List<HistoricalActivityEntity>
}
