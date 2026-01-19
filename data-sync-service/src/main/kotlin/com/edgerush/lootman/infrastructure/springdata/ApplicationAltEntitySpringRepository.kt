package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.ApplicationAltEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for ApplicationAltEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface ApplicationAltEntitySpringRepository :
    CrudRepository<ApplicationAltEntity, Long>,
    PagingAndSortingRepository<ApplicationAltEntity, Long> {

    fun findByApplicationId(applicationId: Long, pageable: Pageable): Page<ApplicationAltEntity>

    fun countByApplicationId(applicationId: Long): Long

    fun findByApplicationId(applicationId: Long): List<ApplicationAltEntity>

    fun deleteByApplicationId(applicationId: Long)
}
