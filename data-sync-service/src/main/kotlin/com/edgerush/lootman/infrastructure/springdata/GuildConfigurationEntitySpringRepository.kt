package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.GuildConfigurationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for GuildConfigurationEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface GuildConfigurationEntitySpringRepository :
    CrudRepository<GuildConfigurationEntity, Long>,
    PagingAndSortingRepository<GuildConfigurationEntity, Long> {
    fun findByGuildId(guildId: String): GuildConfigurationEntity?

    fun existsByGuildId(guildId: String): Boolean

    @Query("SELECT * FROM guild_configurations WHERE sync_enabled = true AND is_active = true")
    fun findAllSyncEnabled(): List<GuildConfigurationEntity>

    @Query("SELECT * FROM guild_configurations WHERE is_active = true")
    fun findAllActive(): List<GuildConfigurationEntity>

    fun findByIsActiveTrue(pageable: Pageable): Page<GuildConfigurationEntity>

    fun countByIsActiveTrue(): Long

    fun findByGuildName(guildName: String): GuildConfigurationEntity?
}
