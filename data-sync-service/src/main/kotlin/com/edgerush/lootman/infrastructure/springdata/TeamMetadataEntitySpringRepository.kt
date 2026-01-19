package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.TeamMetadataEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for TeamMetadataEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface TeamMetadataEntitySpringRepository :
    CrudRepository<TeamMetadataEntity, Long>,
    PagingAndSortingRepository<TeamMetadataEntity, Long> {

    fun findByGuildId(guildId: Long, pageable: Pageable): Page<TeamMetadataEntity>

    fun countByGuildId(guildId: Long): Long

    fun findByGuildId(guildId: Long): List<TeamMetadataEntity>

    fun findByTeamId(teamId: Long): TeamMetadataEntity?

    fun findByGuildIdAndTeamId(guildId: Long, teamId: Long): TeamMetadataEntity?

    fun findByRegion(region: String, pageable: Pageable): Page<TeamMetadataEntity>

    fun countByRegion(region: String): Long

    fun existsByTeamId(teamId: Long): Boolean

    fun deleteByTeamId(teamId: Long)
}
