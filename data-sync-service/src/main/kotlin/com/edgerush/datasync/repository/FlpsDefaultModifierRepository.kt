package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface FlpsDefaultModifierRepository : CrudRepository<FlpsDefaultModifierEntity, Long>, PagingAndSortingRepository<FlpsDefaultModifierEntity, Long> {
    fun findByCategory(category: String, pageable: Pageable): Page<FlpsDefaultModifierEntity>
    fun findByCategoryAndModifierKey(category: String, modifierKey: String): FlpsDefaultModifierEntity?
}