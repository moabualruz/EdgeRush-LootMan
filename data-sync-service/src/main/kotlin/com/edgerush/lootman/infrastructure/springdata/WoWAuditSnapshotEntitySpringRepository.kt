package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.WoWAuditSnapshotEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for WoWAuditSnapshotEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface WoWAuditSnapshotEntitySpringRepository :
    CrudRepository<WoWAuditSnapshotEntity, Long>,
    PagingAndSortingRepository<WoWAuditSnapshotEntity, Long> {
    fun findByEndpoint(
        endpoint: String,
        pageable: Pageable,
    ): Page<WoWAuditSnapshotEntity>

    fun countByEndpoint(endpoint: String): Long

    fun findByEndpoint(endpoint: String): List<WoWAuditSnapshotEntity>
}
