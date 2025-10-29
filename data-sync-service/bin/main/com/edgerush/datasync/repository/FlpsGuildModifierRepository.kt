package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FlpsGuildModifierRepository : CrudRepository<FlpsGuildModifierEntity, Long> {
    fun findByGuildId(guildId: String): List<FlpsGuildModifierEntity>
    fun findByGuildIdAndCategory(guildId: String, category: String): List<FlpsGuildModifierEntity>
    fun findByGuildIdAndCategoryAndModifierKey(guildId: String, category: String, modifierKey: String): FlpsGuildModifierEntity?
}