package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FlpsDefaultModifierRepository : CrudRepository<FlpsDefaultModifierEntity, Long> {
    fun findByCategory(category: String): List<FlpsDefaultModifierEntity>
    fun findByCategoryAndModifierKey(category: String, modifierKey: String): FlpsDefaultModifierEntity?
}