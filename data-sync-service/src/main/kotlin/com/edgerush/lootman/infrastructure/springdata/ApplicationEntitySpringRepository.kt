package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.ApplicationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for ApplicationEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface ApplicationEntitySpringRepository :
    CrudRepository<ApplicationEntity, Long>,
    PagingAndSortingRepository<ApplicationEntity, Long> {
    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<ApplicationEntity>

    fun countByStatus(status: String): Long

    fun findByDiscordId(discordId: String): List<ApplicationEntity>

    @Query(
        """
        SELECT * FROM applications
        WHERE main_character_name = :characterName AND main_character_realm = :realm
        """,
    )
    fun findByMainCharacter(
        characterName: String,
        realm: String,
    ): List<ApplicationEntity>
}
