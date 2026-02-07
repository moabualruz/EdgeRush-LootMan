package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.SyncRunEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for SyncRunEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface SyncRunEntitySpringRepository :
    CrudRepository<SyncRunEntity, Long>,
    PagingAndSortingRepository<SyncRunEntity, Long> {
    fun findBySource(
        source: String,
        pageable: Pageable,
    ): Page<SyncRunEntity>

    fun countBySource(source: String): Long

    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<SyncRunEntity>

    fun countByStatus(status: String): Long

    fun findBySource(source: String): List<SyncRunEntity>
}
