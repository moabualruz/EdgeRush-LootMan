package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for FlpsDefaultModifierEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface FlpsDefaultModifierEntitySpringRepository :
    CrudRepository<FlpsDefaultModifierEntity, Long>,
    PagingAndSortingRepository<FlpsDefaultModifierEntity, Long> {
    fun findByModifierKey(modifierKey: String): FlpsDefaultModifierEntity?

    fun existsByModifierKey(modifierKey: String): Boolean

    fun findByCategory(
        category: String,
        pageable: Pageable,
    ): Page<FlpsDefaultModifierEntity>

    fun countByCategory(category: String): Long
}
