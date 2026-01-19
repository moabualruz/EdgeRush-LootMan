package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for ApplicationQuestionEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface ApplicationQuestionEntitySpringRepository :
    CrudRepository<ApplicationQuestionEntity, Long>,
    PagingAndSortingRepository<ApplicationQuestionEntity, Long> {

    fun findByApplicationId(applicationId: Long, pageable: Pageable): Page<ApplicationQuestionEntity>

    fun countByApplicationId(applicationId: Long): Long

    fun findByApplicationId(applicationId: Long): List<ApplicationQuestionEntity>

    fun findByApplicationIdAndPosition(applicationId: Long, position: Int): ApplicationQuestionEntity?

    fun deleteByApplicationId(applicationId: Long)
}
