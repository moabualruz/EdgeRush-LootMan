package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for ApplicationQuestionFileEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface ApplicationQuestionFileEntitySpringRepository :
    CrudRepository<ApplicationQuestionFileEntity, Long>,
    PagingAndSortingRepository<ApplicationQuestionFileEntity, Long> {

    fun findByApplicationId(applicationId: Long, pageable: Pageable): Page<ApplicationQuestionFileEntity>

    fun countByApplicationId(applicationId: Long): Long

    fun findByApplicationId(applicationId: Long): List<ApplicationQuestionFileEntity>

    fun findByApplicationIdAndQuestionPosition(applicationId: Long, questionPosition: Int): List<ApplicationQuestionFileEntity>

    fun deleteByApplicationId(applicationId: Long)
}
