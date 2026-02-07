package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RecruitmentApplicationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RecruitmentApplicationEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RecruitmentApplicationEntitySpringRepository :
    CrudRepository<RecruitmentApplicationEntity, String>,
    PagingAndSortingRepository<RecruitmentApplicationEntity, String> {
    fun findByGuildId(
        guildId: String,
        pageable: Pageable,
    ): Page<RecruitmentApplicationEntity>

    fun countByGuildId(guildId: String): Long

    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<RecruitmentApplicationEntity>

    fun countByStatus(status: String): Long

    @Query(
        """
        SELECT * FROM enhanced_applications
        WHERE guild_id = :guildId AND status = :status
        ORDER BY created_at DESC
        """,
    )
    fun findByGuildIdAndStatusQuery(
        guildId: String,
        status: String,
    ): List<RecruitmentApplicationEntity>

    // Derived query method for pagination (without @Query)
    fun findByGuildIdAndStatus(
        guildId: String,
        status: String,
        pageable: Pageable,
    ): Page<RecruitmentApplicationEntity>

    @Query(
        """
        SELECT COUNT(*) FROM enhanced_applications
        WHERE guild_id = :guildId AND status = :status
        """,
    )
    fun countByGuildIdAndStatus(
        guildId: String,
        status: String,
    ): Long

    fun findByDiscordId(discordId: String): List<RecruitmentApplicationEntity>

    fun findByBattleNetId(battleNetId: String): List<RecruitmentApplicationEntity>

    @Query(
        """
        SELECT * FROM enhanced_applications
        WHERE character_name = :characterName AND character_realm = :characterRealm
        """,
    )
    fun findByCharacterNameAndRealm(
        characterName: String,
        characterRealm: String,
    ): List<RecruitmentApplicationEntity>
}
