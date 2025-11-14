package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface FlpsGuildModifierRepository : CrudRepository<FlpsGuildModifierEntity, Long>, PagingAndSortingRepository<FlpsGuildModifierEntity, Long> {
    fun findByGuildId(
        guildId: String,
        pageable: Pageable,
    ): Page<FlpsGuildModifierEntity>

    fun findByGuildIdAndCategory(
        guildId: String,
        category: String,
        pageable: Pageable,
    ): Page<FlpsGuildModifierEntity>

    fun findByGuildIdAndCategoryAndModifierKey(
        guildId: String,
        category: String,
        modifierKey: String,
    ): FlpsGuildModifierEntity?
}
