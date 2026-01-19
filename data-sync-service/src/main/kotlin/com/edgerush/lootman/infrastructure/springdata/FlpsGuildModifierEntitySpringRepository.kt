package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for FlpsGuildModifierEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface FlpsGuildModifierEntitySpringRepository :
    CrudRepository<FlpsGuildModifierEntity, Long>,
    PagingAndSortingRepository<FlpsGuildModifierEntity, Long> {

    fun findByGuildId(guildId: String, pageable: Pageable): Page<FlpsGuildModifierEntity>

    fun countByGuildId(guildId: String): Long

    fun findByGuildId(guildId: String): List<FlpsGuildModifierEntity>

    fun findByGuildIdAndModifierKey(guildId: String, modifierKey: String): FlpsGuildModifierEntity?

    fun findByGuildIdAndCategory(guildId: String, category: String, pageable: Pageable): Page<FlpsGuildModifierEntity>

    fun countByGuildIdAndCategory(guildId: String, category: String): Long

    fun deleteByGuildId(guildId: String)
}
